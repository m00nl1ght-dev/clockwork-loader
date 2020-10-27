package dev.m00nl1ght.clockwork.extension.security.internal;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

public final class ProviderImpl extends Provider {

    public static final String NAME = "Clockwork";
    public static final String VERSION = "1.0";
    public static final String INFO = "Provides a Policy implementation for the Clockwork plugin framework.";

    public ProviderImpl() {
        super(NAME, VERSION, INFO);
        putService(new PolicyProviderService(this));
    }

    private static final class PolicyProviderService extends Provider.Service {

        private PolicyProviderService(Provider provider) {
            super(provider, "Policy", PolicyImpl.NAME, PolicyImpl.class.getName(), null, null);
        }

        @Override
        public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException {
            try {
                return new PolicyImpl();
            } catch (Exception e) {
                throw new NoSuchAlgorithmException("Error constructing Policy object", e);
            }
        }

    }

}
