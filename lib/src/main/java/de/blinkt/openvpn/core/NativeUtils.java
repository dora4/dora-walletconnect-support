/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Build;

import java.security.InvalidKeyException;

public class NativeUtils {
    public static native byte[] rsasign(byte[] input, int pkey, boolean pkcs1padding) throws InvalidKeyException;

    public static native String[] getIfconfig() throws IllegalArgumentException;

    static native void jniclose(int fdint);

    public static String getNativeAPI() {
        if (isRoboUnitTest())
            return "ROBO";
        else
            return getJNIAPI();
    }

    private static native String getJNIAPI();

    public static native String getOpenVPN2GitVersion();

    public static native String getOpenVPN3GitVersion();

    static boolean rsspssloaded = false;

    public static byte[] addRssPssPadding(int hashtype, int MSBits, int rsa_size, byte[] from)
    {
        if (!rsspssloaded) {
            rsspssloaded = true;
            System.loadLibrary("rsapss");
        }

        return rsapss(hashtype, MSBits, rsa_size, from);
    }

    private static native byte[] rsapss(int hashtype, int MSBits, int rsa_size, byte[] from);

    public final static int[] openSSLlengths = {
        16, 64, 256, 1024, 1500, 8 * 1024, 16 * 1024
    };

    public static native double[] getOpenSSLSpeed(String algorithm, int testnum);

    static {
        if (!isRoboUnitTest()) {
            System.loadLibrary("ovpnutil");
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
                System.loadLibrary("jbcrypto");


//            if (!BuildConfig.FLAVOR.equals("skeleton")) {
//                System.loadLibrary("osslspeedtest");
//            }
        }
    }

    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT); }

}