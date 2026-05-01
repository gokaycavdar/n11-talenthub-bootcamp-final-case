package com.gokaycavdar.paymentservice.service.provider;

import com.gokaycavdar.paymentservice.dto.payment.ThreeDsCallbackRequest;
import com.gokaycavdar.paymentservice.entity.PaymentProviderType;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    PaymentProviderInitResult initiate3ds(PaymentProviderInitRequest request);

    PaymentProviderCallbackResult resolveCallback(ThreeDsCallbackRequest request);
}
