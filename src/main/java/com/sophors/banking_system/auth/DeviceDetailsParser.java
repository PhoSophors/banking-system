package com.sophors.banking_system.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DeviceDetailsParser {

    private DeviceDetailsParser() {
    }

    public static DeviceDetails parse(String userAgent) {
        String ua = userAgent == null ? "" : userAgent;

        String deviceType = detectDeviceType(ua);
        String deviceName = detectDeviceName(ua);
        String deviceVersion = detectDeviceVersion(ua);
        String osName = detectOsName(ua);
        String osVersion = detectOsVersion(ua, osName);
        String browserName = detectBrowserName(ua);
        String browserVersion = detectBrowserVersion(ua, browserName);

        return new DeviceDetails(
                deviceType,
                deviceName,
                deviceVersion,
                osName,
                osVersion,
                browserName,
                browserVersion
        );
    }

    private static String detectDeviceType(String ua) {
        if (ua.contains("iPad") || ua.contains("Tablet")) {
            return "Tablet";
        }
        if (ua.contains("Mobi") || ua.contains("Android") || ua.contains("iPhone")) {
            return "Mobile";
        }
        return "Desktop";
    }

    private static String detectDeviceName(String ua) {
        if (ua.contains("iPhone")) {
            return "iPhone";
        }
        if (ua.contains("iPad")) {
            return "iPad";
        }
        if (ua.contains("Macintosh")) {
            return "Mac";
        }
        if (ua.contains("Windows")) {
            return "Windows PC";
        }
        if (ua.contains("Android")) {
            String model = extract(ua, ";\\s*([^;)]*Build/[^;)]*)");
            if (model != null) {
                return model.replaceAll("\\s*Build/.*$", "").trim();
            }
            return "Android Device";
        }
        if (ua.contains("Linux")) {
            return "Linux PC";
        }
        return "Unknown Device";
    }

    private static String detectDeviceVersion(String ua) {
        if (ua.contains("iPhone")) {
            return extractAndNormalize(ua, "iPhone OS ([0-9_]+)");
        }
        if (ua.contains("iPad")) {
            return extractAndNormalize(ua, "CPU OS ([0-9_]+)");
        }
        if (ua.contains("Android")) {
            return extractAndNormalize(ua, "Android ([0-9.]+)");
        }
        if (ua.contains("Windows NT")) {
            return mapWindowsVersion(extract(ua, "Windows NT ([0-9.]+)"));
        }
        if (ua.contains("Mac OS X")) {
            return extractAndNormalize(ua, "Mac OS X ([0-9_]+)");
        }
        return "Unknown";
    }

    private static String detectOsName(String ua) {
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) {
            return "macOS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    private static String detectOsVersion(String ua, String osName) {
        return switch (osName) {
            case "iOS" -> firstNonNull(
                    extractAndNormalize(ua, "iPhone OS ([0-9_]+)"),
                    extractAndNormalize(ua, "CPU OS ([0-9_]+)")
            );
            case "Android" -> extractAndNormalize(ua, "Android ([0-9.]+)");
            case "Windows" -> mapWindowsVersion(extract(ua, "Windows NT ([0-9.]+)"));
            case "macOS" -> extractAndNormalize(ua, "Mac OS X ([0-9_]+)");
            default -> "Unknown";
        };
    }

    private static String detectBrowserName(String ua) {
        if (ua.contains("Edg/")) {
            return "Edge";
        }
        if (ua.contains("OPR/") || ua.contains("Opera/")) {
            return "Opera";
        }
        if (ua.contains("Chrome/") && !ua.contains("Edg/")) {
            return "Chrome";
        }
        if (ua.contains("Firefox/")) {
            return "Firefox";
        }
        if (ua.contains("Safari/") && ua.contains("Version/") && !ua.contains("Chrome/")) {
            return "Safari";
        }
        return "Unknown";
    }

    private static String detectBrowserVersion(String ua, String browserName) {
        return switch (browserName) {
            case "Edge" -> extract(ua, "Edg/([0-9.]+)");
            case "Opera" -> firstNonNull(extract(ua, "OPR/([0-9.]+)"), extract(ua, "Opera/([0-9.]+)"));
            case "Chrome" -> extract(ua, "Chrome/([0-9.]+)");
            case "Firefox" -> extract(ua, "Firefox/([0-9.]+)");
            case "Safari" -> extract(ua, "Version/([0-9.]+)");
            default -> "Unknown";
        };
    }

    private static String mapWindowsVersion(String version) {
        if (version == null) {
            return "Unknown";
        }
        return switch (version) {
            case "10.0" -> "10/11";
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            default -> version;
        };
    }

    private static String extractAndNormalize(String input, String regex) {
        String value = extract(input, regex);
        if (value == null) {
            return "Unknown";
        }
        return value.replace('_', '.');
    }

    private static String extract(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String firstNonNull(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return "Unknown";
    }
}
