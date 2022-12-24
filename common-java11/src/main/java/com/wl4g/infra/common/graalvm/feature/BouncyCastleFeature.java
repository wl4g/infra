package com.wl4g.infra.common.graalvm.feature;

import java.security.Security;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jcajce.provider.drbg.DRBG;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import com.oracle.svm.core.annotate.AutomaticFeature;

/**
 * {@link BouncyCastleFeature}
 * 
 * @author James Wong
 * @version 2022-12-25
 * @since https://github.com/oracle/graal/issues/2800
 */
@AutomaticFeature
public class BouncyCastleFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        // https://www.graalvm.org/22.0/reference-manual/native-image/JCASecurityServices/
        // Provider bcProvider = Security.getProvider("BC");
        // Security.removeProvider("BC");
        // Security.insertProviderAt(bcProvider, 1);

        RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
        RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$Default", "");
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV", "");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        // programmatically re-run class initialization at runtime
        // (equivalent to the (deprecated)
        // --rerun-class-initialization-at-runtime=
        // command-line option for GraalVM's native-image command)
        RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
        // all org.bouncycastle packages are initialized at build time,
        // but some specific classes need be re-initialized at runtime
        // due to static SecureRandom seeding
        rci.rerunInitialization(CryptoServicesRegistrar.class,
                "See https://github.com/micronaut-projects/micronaut-oracle-cloud/pull/17#discussion_r472955378");
        rci.rerunInitialization(DRBG.Default.class,
                "See https://github.com/micronaut-projects/micronaut-oracle-cloud/pull/17#discussion_r472955378");
        rci.rerunInitialization(DRBG.NonceAndIV.class,
                "See https://github.com/micronaut-projects/micronaut-oracle-cloud/pull/17#discussion_r472955378");
    }
}