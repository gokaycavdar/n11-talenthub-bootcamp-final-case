export default function ThreeDsRunner({
                                          htmlContent,
                                          title = "3DS Odeme Akisi",
                                      }) {
    if (!htmlContent) {
        return null;
    }

    return (
        <section className="three-ds-runner">
            <div className="three-ds-runner__content">
                <span className="eyebrow">3DS Session</span>
                <h2>{title}</h2>
                <p>
                    Mock 3DS formu arka planda otomatik gonderiliyor. Siparis durumu
                    backend tarafinda takip ediliyor.
                </p>
            </div>

            <iframe
                title="three-ds-runner"
                srcDoc={htmlContent}
                className="three-ds-runner__frame"
            />
        </section>
    );
}
