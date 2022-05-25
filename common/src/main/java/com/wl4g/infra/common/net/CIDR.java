package com.wl4g.infra.common.net;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;

/**
 * Such source code comes from:
 * {@link com.wl4g.infra.common.net.netty.handler.ipfilter.CIDR}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public abstract class CIDR implements Comparable<CIDR> {

    /** The base address of the CIDR notation */
    protected InetAddress baseAddress;

    /** The mask used in the CIDR notation */
    protected int cidrMask;

    /**
     * Create CIDR using the CIDR Notation
     *
     * @return the generated CIDR
     */
    public static CIDR newCIDR(InetAddress baseAddress, int cidrMask) throws IllegalArgumentException {
        if (cidrMask < 0) {
            throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
        }
        if (baseAddress instanceof Inet4Address) {
            if (cidrMask > 32) {
                throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
            }
            return new CIDR4((Inet4Address) baseAddress, cidrMask);
        }
        // IPv6.
        if (cidrMask > 128) {
            throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
        }
        return new CIDR6((Inet6Address) baseAddress, cidrMask);
    }

    /**
     * Create CIDR using the normal Notation
     *
     * @return the generated CIDR
     */
    public static CIDR newCIDR(InetAddress baseAddress, String scidrMask) throws IllegalArgumentException {
        int cidrMask = getNetMask(scidrMask);
        if (cidrMask < 0) {
            throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
        }
        if (baseAddress instanceof Inet4Address) {
            if (cidrMask > 32) {
                throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
            }
            return new CIDR4((Inet4Address) baseAddress, cidrMask);
        }
        cidrMask += 96;
        // IPv6.
        if (cidrMask > 128) {
            throw new IllegalArgumentException("Invalid mask length used: " + cidrMask);
        }
        return new CIDR6((Inet6Address) baseAddress, cidrMask);
    }

    /**
     * Create CIDR using the CIDR or normal Notation<BR>
     * i.e.: CIDR subnet = newCIDR ("10.10.10.0/24"); or CIDR subnet = newCIDR
     * ("1fff:0:0a88:85a3:0:0:ac1f:8001/24"); or CIDR subnet = newCIDR
     * ("10.10.10.0/255.255.255.0");
     *
     * @return the generated CIDR
     */
    public static CIDR newCIDR(String cidr) throws UnknownHostException {
        int p = cidr.indexOf('/');
        if (p < 0) {
            throw new IllegalArgumentException("Invalid CIDR notation used: " + cidr);
        }
        String addrString = cidr.substring(0, p);
        String maskString = cidr.substring(p + 1);
        InetAddress addr = addressStringToInet(addrString);
        int mask;
        if (maskString.indexOf('.') < 0) {
            mask = parseInt(maskString, -1);
        } else {
            mask = getNetMask(maskString);
            if (addr instanceof Inet6Address) {
                mask += 96;
            }
        }
        if (mask < 0) {
            throw new IllegalArgumentException("Invalid mask length used: " + maskString);
        }
        return newCIDR(addr, mask);
    }

    /** @return the baseAddress of the CIDR block. */
    public InetAddress getBaseAddress() {
        return baseAddress;
    }

    /** @return the Mask length. */
    public int getMask() {
        return cidrMask;
    }

    /** @return the textual CIDR notation. */
    @Override
    public String toString() {
        return baseAddress.getHostAddress() + '/' + cidrMask;
    }

    /** @return the end address of this block. */
    public abstract InetAddress getEndAddress();

    /**
     * Compares the given InetAddress against the CIDR and returns true if the
     * ip is in the subnet-ip-range and false if not.
     *
     * @return returns true if the given IP address is inside the currently set
     *         network.
     */
    public abstract boolean contains(InetAddress inetAddress);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CIDR)) {
            return false;
        }
        return compareTo((CIDR) o) == 0;
    }

    @Override
    public int hashCode() {
        return baseAddress.hashCode();
    }

    /**
     * Convert an IPv4 or IPv6 textual representation into an InetAddress.
     *
     * @return the created InetAddress
     * @throws UnknownHostException
     */
    private static InetAddress addressStringToInet(String addr) throws UnknownHostException {
        return InetAddress.getByName(addr);
    }

    /**
     * Get the Subnet's Netmask in Decimal format.<BR>
     * i.e.: getNetMask("255.255.255.0") returns the integer CIDR mask
     *
     * @param netMask
     *            a network mask
     * @return the integer CIDR mask
     */
    private static int getNetMask(String netMask) {
        StringTokenizer nm = new StringTokenizer(netMask, ".");
        int i = 0;
        int[] netmask = new int[4];
        while (nm.hasMoreTokens()) {
            netmask[i] = Integer.parseInt(nm.nextToken());
            i++;
        }
        int mask1 = 0;
        for (i = 0; i < 4; i++) {
            mask1 += Integer.bitCount(netmask[i]);
        }
        return mask1;
    }

    /**
     * @param intstr
     *            a string containing an integer.
     * @param def
     *            the default if the string does not contain a valid integer.
     * @return the inetAddress from the integer
     */
    private static int parseInt(String intstr, int def) {
        Integer res;
        if (intstr == null) {
            return def;
        }
        try {
            res = Integer.decode(intstr);
        } catch (Exception e) {
            res = def;
        }
        return res.intValue();
    }

    /**
     * Compute a byte representation of IpV4 from a IpV6
     *
     * @return the byte representation
     * @throws IllegalArgumentException
     *             if the IpV6 cannot be mapped to IpV4
     */
    public static byte[] getIpV4FromIpV6(Inet6Address address) {
        byte[] baddr = address.getAddress();
        for (int i = 0; i < 9; i++) {
            if (baddr[i] != 0) {
                throw new IllegalArgumentException("This IPv6 address cannot be used in IPv4 context");
            }
        }
        if (baddr[10] != 0 && baddr[10] != 0xFF || baddr[11] != 0 && baddr[11] != 0xFF) {
            throw new IllegalArgumentException("This IPv6 address cannot be used in IPv4 context");
        }
        return new byte[] { baddr[12], baddr[13], baddr[14], baddr[15] };
    }

    /**
     * Compute a byte representation of IpV6 from a IpV4
     *
     * @return the byte representation
     * @throws IllegalArgumentException
     *             if the IpV6 cannot be mapped to IpV4
     */
    public static byte[] getIpV6FromIpV4(Inet4Address address) {
        byte[] baddr = address.getAddress();
        return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, baddr[0], baddr[1], baddr[2], baddr[3] };
    }

    static class CIDR4 extends CIDR {
        /** The integer for the base address */
        private int addressInt;

        /** The integer for the end address */
        private final int addressEndInt;

        protected CIDR4(Inet4Address newaddr, int mask) {
            cidrMask = mask;
            addressInt = ipv4AddressToInt(newaddr);
            int newmask = ipv4PrefixLengthToMask(mask);
            addressInt &= newmask;
            try {
                baseAddress = intToIPv4Address(addressInt);
            } catch (UnknownHostException e) {
                // this should never happen
            }
            addressEndInt = addressInt + ipv4PrefixLengthToLength(cidrMask) - 1;
        }

        @Override
        public InetAddress getEndAddress() {
            try {
                return intToIPv4Address(addressEndInt);
            } catch (UnknownHostException e) {
                // this should never happen
                return null;
            }
        }

        public int compareTo(CIDR arg) {
            if (arg instanceof CIDR6) {
                byte[] address = getIpV4FromIpV6((Inet6Address) arg.baseAddress);
                int net = ipv4AddressToInt(address);
                if (net == addressInt && arg.cidrMask == cidrMask) {
                    return 0;
                }
                if (net < addressInt) {
                    return 1;
                }
                if (net > addressInt) {
                    return -1;
                }
                if (arg.cidrMask < cidrMask) {
                    return -1;
                }
                return 1;
            }
            CIDR4 o = (CIDR4) arg;
            if (o.addressInt == addressInt && o.cidrMask == cidrMask) {
                return 0;
            }
            if (o.addressInt < addressInt) {
                return 1;
            }
            if (o.addressInt > addressInt) {
                return -1;
            }
            if (o.cidrMask < cidrMask) {
                // greater Mask means less IpAddresses so -1
                return -1;
            }
            return 1;
        }

        @Override
        public boolean contains(InetAddress inetAddress) {
            int search = ipv4AddressToInt(inetAddress);
            return search >= addressInt && search <= addressEndInt;
        }

        /**
         * Given an IPv4 baseAddress length, return the block length. I.e., a
         * baseAddress length of 24 will return 256.
         */
        private static int ipv4PrefixLengthToLength(int prefixLength) {
            return 1 << 32 - prefixLength;
        }

        /**
         * Given a baseAddress length, return a netmask. I.e, a baseAddress
         * length of 24 will return 0xFFFFFF00.
         */
        private static int ipv4PrefixLengthToMask(int prefixLength) {
            return ~((1 << 32 - prefixLength) - 1);
        }

        /**
         * Convert an integer into an (IPv4) InetAddress.
         *
         * @return the created InetAddress
         */
        private static InetAddress intToIPv4Address(int addr) throws UnknownHostException {
            byte[] a = new byte[4];
            a[0] = (byte) (addr >> 24 & 0xFF);
            a[1] = (byte) (addr >> 16 & 0xFF);
            a[2] = (byte) (addr >> 8 & 0xFF);
            a[3] = (byte) (addr & 0xFF);
            return InetAddress.getByAddress(a);
        }

        /**
         * Given an IPv4 address, convert it into an integer.
         *
         * @return the integer representation of the InetAddress
         * @throws IllegalArgumentException
         *             if the address is really an IPv6 address.
         */
        private static int ipv4AddressToInt(InetAddress addr) {
            byte[] address;
            if (addr instanceof Inet6Address) {
                address = getIpV4FromIpV6((Inet6Address) addr);
            } else {
                address = addr.getAddress();
            }
            return ipv4AddressToInt(address);
        }

        /**
         * Given an IPv4 address as array of bytes, convert it into an integer.
         *
         * @return the integer representation of the InetAddress
         * @throws IllegalArgumentException
         *             if the address is really an IPv6 address.
         */
        private static int ipv4AddressToInt(byte[] address) {
            int net = 0;
            for (byte addres : address) {
                net <<= 8;
                net |= addres & 0xFF;
            }
            return net;
        }
    }

    @Slf4j
    static class CIDR6 extends CIDR {

        /** The big integer for the base address */
        private BigInteger addressBigInt;

        /** The big integer for the end address */
        private final BigInteger addressEndBigInt;

        protected CIDR6(Inet6Address newaddress, int newmask) {
            cidrMask = newmask;
            addressBigInt = ipv6AddressToBigInteger(newaddress);
            BigInteger mask = ipv6CidrMaskToMask(newmask);
            try {
                addressBigInt = addressBigInt.and(mask);
                baseAddress = bigIntToIPv6Address(addressBigInt);
            } catch (UnknownHostException e) {
                // this should never happen.
            }
            addressEndBigInt = addressBigInt.add(ipv6CidrMaskToBaseAddress(cidrMask)).subtract(BigInteger.ONE);
        }

        @Override
        public InetAddress getEndAddress() {
            try {
                return bigIntToIPv6Address(addressEndBigInt);
            } catch (UnknownHostException e) {
                if (log.isErrorEnabled()) {
                    log.error("invalid ip address calculated as an end address");
                }
                return null;
            }
        }

        public int compareTo(CIDR arg) {
            if (arg instanceof CIDR4) {
                BigInteger net = ipv6AddressToBigInteger(arg.baseAddress);
                int res = net.compareTo(addressBigInt);
                if (res == 0) {
                    if (arg.cidrMask == cidrMask) {
                        return 0;
                    }
                    if (arg.cidrMask < cidrMask) {
                        return -1;
                    }
                    return 1;
                }
                return res;
            }
            CIDR6 o = (CIDR6) arg;
            if (o.addressBigInt.equals(addressBigInt) && o.cidrMask == cidrMask) {
                return 0;
            }
            int res = o.addressBigInt.compareTo(addressBigInt);
            if (res == 0) {
                if (o.cidrMask < cidrMask) {
                    // greater Mask means less IpAddresses so -1
                    return -1;
                }
                return 1;
            }
            return res;
        }

        @Override
        public boolean contains(InetAddress inetAddress) {
            BigInteger search = ipv6AddressToBigInteger(inetAddress);
            return search.compareTo(addressBigInt) >= 0 && search.compareTo(addressEndBigInt) <= 0;
        }

        /**
         * Given an IPv6 baseAddress length, return the block length. I.e., a
         * baseAddress length of 96 will return 2**32.
         */
        private static BigInteger ipv6CidrMaskToBaseAddress(int cidrMask) {
            return BigInteger.ONE.shiftLeft(128 - cidrMask);
        }

        private static BigInteger ipv6CidrMaskToMask(int cidrMask) {
            return BigInteger.ONE.shiftLeft(128 - cidrMask).subtract(BigInteger.ONE).not();
        }

        /**
         * Given an IPv6 address, convert it into a BigInteger.
         *
         * @return the integer representation of the InetAddress
         * @throws IllegalArgumentException
         *             if the address is not an IPv6 address.
         */
        private static BigInteger ipv6AddressToBigInteger(InetAddress addr) {
            byte[] ipv6;
            if (addr instanceof Inet4Address) {
                ipv6 = getIpV6FromIpV4((Inet4Address) addr);
            } else {
                ipv6 = addr.getAddress();
            }
            if (ipv6[0] == -1) {
                return new BigInteger(1, ipv6);
            }
            return new BigInteger(ipv6);
        }

        /**
         * Convert a big integer into an IPv6 address.
         *
         * @return the inetAddress from the integer
         * @throws IllegalArgumentException
         *             if the big integer is too large, and thus an invalid IPv6
         *             address.
         * @throws UnknownHostException
         */
        private static InetAddress bigIntToIPv6Address(BigInteger addr) throws UnknownHostException {
            byte[] a = new byte[16];
            byte[] b = addr.toByteArray();
            if (b.length > 16 && !(b.length == 17 && b[0] == 0)) {
                throw new IllegalArgumentException("invalid IPv6 address (too big)");
            }
            if (b.length == 16) {
                return InetAddress.getByAddress(b);
            }
            // handle the case where the IPv6 address starts with "FF".
            if (b.length == 17) {
                System.arraycopy(b, 1, a, 0, 16);
            } else {
                // copy the address into a 16 byte array, zero-filled.
                int p = 16 - b.length;
                System.arraycopy(b, 0, a, p, b.length);
            }
            return InetAddress.getByAddress(a);
        }
    }

}
