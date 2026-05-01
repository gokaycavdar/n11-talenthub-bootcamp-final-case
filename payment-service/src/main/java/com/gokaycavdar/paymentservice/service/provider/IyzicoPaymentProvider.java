package com.gokaycavdar.paymentservice.service.provider;

import com.gokaycavdar.paymentservice.config.IyzicoProperties;
import com.gokaycavdar.paymentservice.dto.payment.PaymentBasketItemRequest;
import com.gokaycavdar.paymentservice.dto.payment.ThreeDsCallbackRequest;
import com.gokaycavdar.paymentservice.entity.PaymentProviderType;
import com.gokaycavdar.paymentservice.exception.BusinessException;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.model.ThreedsInitialize;
import com.iyzipay.model.ThreedsPayment;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.CreateThreedsPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IyzicoPaymentProvider implements PaymentProvider {

    private static final String DEFAULT_GSM_NUMBER = "+905350000000";
    private static final String DEFAULT_IDENTITY_NUMBER = "74300864791";
    private static final String DEFAULT_COUNTRY = "Turkey";
    private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    private static final DateTimeFormatter IYZICO_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IyzicoProperties iyzicoProperties;

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.IYZICO;
    }

    @Override
    public PaymentProviderInitResult initiate3ds(PaymentProviderInitRequest request) {
        validateConfiguration();

        CreatePaymentRequest iyzicoRequest = new CreatePaymentRequest();
        iyzicoRequest.setLocale(Locale.TR.getValue());
        iyzicoRequest.setConversationId(request.conversationId());
        iyzicoRequest.setPrice(request.price());
        iyzicoRequest.setPaidPrice(request.paidPrice());
        iyzicoRequest.setCurrency(Currency.TRY.name());
        iyzicoRequest.setInstallment(1);
        iyzicoRequest.setBasketId(String.valueOf(request.orderId()));
        iyzicoRequest.setPaymentChannel(PaymentChannel.WEB.name());
        iyzicoRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());
        iyzicoRequest.setCallbackUrl(request.callbackUrl());

        iyzicoRequest.setPaymentCard(buildPaymentCard(request));
        iyzicoRequest.setBuyer(buildBuyer(request));
        iyzicoRequest.setShippingAddress(buildAddress(
                request.shippingFullName(),
                request.shippingAddressLine(),
                request.city(),
                request.postalCode()
        ));
        iyzicoRequest.setBillingAddress(buildAddress(
                request.shippingFullName(),
                request.shippingAddressLine(),
                request.city(),
                request.postalCode()
        ));
        iyzicoRequest.setBasketItems(buildBasketItems(request.basketItems()));

        ThreedsInitialize response = ThreedsInitialize.create(iyzicoRequest, buildOptions());

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            String errorMessage = StringUtils.hasText(response.getErrorMessage())
                    ? response.getErrorMessage()
                    : "Iyzico 3DS initialize failed";
            throw new BusinessException(errorMessage);
        }

        String htmlContent = decodeHtmlContent(response.getHtmlContent());

        log.info("Iyzico 3DS initialize succeeded. conversationId={}, orderId={}",
                request.conversationId(), request.orderId());

        return new PaymentProviderInitResult(htmlContent);
    }

    @Override
    public PaymentProviderCallbackResult resolveCallback(ThreeDsCallbackRequest request) {
        validateConfiguration();

        if (!"success".equalsIgnoreCase(request.getStatus())) {
            return new PaymentProviderCallbackResult(
                    false,
                    request.getPaymentId(),
                    StringUtils.hasText(request.getReason())
                            ? request.getReason()
                            : "Iyzico callback status is failure"
            );
        }

        if (!"1".equals(request.getMdStatus())) {
            return new PaymentProviderCallbackResult(
                    false,
                    request.getPaymentId(),
                    mapMdStatusFailureReason(request.getMdStatus())
            );
        }

        CreateThreedsPaymentRequest authRequest = new CreateThreedsPaymentRequest();
        authRequest.setLocale(Locale.TR.getValue());
        authRequest.setConversationId(request.getConversationId());
        authRequest.setPaymentId(request.getPaymentId());

        if (StringUtils.hasText(request.getConversationData())) {
            authRequest.setConversationData(request.getConversationData());
        }

        ThreedsPayment authResponse = ThreedsPayment.create(authRequest, buildOptions());

        if ("success".equalsIgnoreCase(authResponse.getStatus())) {
            return new PaymentProviderCallbackResult(
                    true,
                    authResponse.getPaymentId(),
                    null
            );
        }

        String errorMessage = StringUtils.hasText(authResponse.getErrorMessage())
                ? authResponse.getErrorMessage()
                : "Iyzico auth 3DS failed";

        return new PaymentProviderCallbackResult(
                false,
                authResponse.getPaymentId(),
                errorMessage
        );
    }

    private Options buildOptions() {
        Options options = new Options();
        options.setApiKey(iyzicoProperties.getApiKey());
        options.setSecretKey(iyzicoProperties.getSecretKey());
        options.setBaseUrl(iyzicoProperties.getBaseUrl());
        return options;
    }

    private PaymentCard buildPaymentCard(PaymentProviderInitRequest request) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(request.cardHolder());
        paymentCard.setCardNumber(request.cardNumber());
        paymentCard.setExpireMonth(request.expireMonth());
        paymentCard.setExpireYear(normalizeExpireYear(request.expireYear()));
        paymentCard.setCvc(request.cvc());
        paymentCard.setRegisterCard(0);
        return paymentCard;
    }

    private Buyer buildBuyer(PaymentProviderInitRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String nowValue = now.format(IYZICO_DATE_FORMATTER);

        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(request.userId()));
        buyer.setName(request.buyerFirstName());
        buyer.setSurname(request.buyerLastName());
        buyer.setGsmNumber(DEFAULT_GSM_NUMBER);
        buyer.setEmail(request.buyerEmail());
        buyer.setIdentityNumber(DEFAULT_IDENTITY_NUMBER);
        buyer.setLastLoginDate(nowValue);
        buyer.setRegistrationDate(nowValue);
        buyer.setRegistrationAddress(request.shippingAddressLine());
        buyer.setIp(DEFAULT_IP_ADDRESS);
        buyer.setCity(request.city());
        buyer.setCountry(DEFAULT_COUNTRY);
        buyer.setZipCode(request.postalCode());
        return buyer;
    }

    private Address buildAddress(
            String contactName,
            String addressLine,
            String city,
            String postalCode
    ) {
        Address address = new Address();
        address.setContactName(contactName);
        address.setCity(city);
        address.setCountry(DEFAULT_COUNTRY);
        address.setAddress(addressLine);
        address.setZipCode(postalCode);
        return address;
    }

    private List<BasketItem> buildBasketItems(List<PaymentBasketItemRequest> items) {
        List<BasketItem> basketItems = new ArrayList<>();

        for (PaymentBasketItemRequest item : items) {
            BasketItem basketItem = new BasketItem();
            basketItem.setId(String.valueOf(item.productId()));
            basketItem.setName(item.productName());
            basketItem.setCategory1("General");
            basketItem.setItemType(BasketItemType.PHYSICAL.name());
            basketItem.setPrice(item.lineTotal());
            basketItems.add(basketItem);
        }

        return basketItems;
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(iyzicoProperties.getApiKey())
                || !StringUtils.hasText(iyzicoProperties.getSecretKey())
                || !StringUtils.hasText(iyzicoProperties.getBaseUrl())) {
            throw new BusinessException("Iyzico configuration is missing");
        }
    }

    private String normalizeExpireYear(String expireYear) {
        if (!StringUtils.hasText(expireYear)) {
            return expireYear;
        }

        if (expireYear.length() == 2) {
            return "20" + expireYear;
        }

        return expireYear;
    }

    private String decodeHtmlContent(String htmlContent) {
        if (!StringUtils.hasText(htmlContent)) {
            return "";
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(htmlContent);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            log.warn("Iyzico html content could not be Base64 decoded, raw content will be returned");
            return htmlContent;
        }
    }

    private String mapMdStatusFailureReason(String mdStatus) {
        return switch (mdStatus == null ? "" : mdStatus) {
            case "0" -> "3DS verification failed: invalid signature or verification";
            case "2" -> "3DS verification failed: card holder or issuer is not registered";
            case "3" -> "3DS verification failed: issuer is not registered";
            case "4" -> "3DS verification failed: card holder chose to register later";
            case "5" -> "3DS verification failed: verification is not possible";
            case "6" -> "3DS verification failed: 3D Secure error";
            case "7" -> "3DS verification failed: system error";
            case "8" -> "3DS verification failed: unknown card";
            default -> "3DS verification failed";
        };
    }
}
