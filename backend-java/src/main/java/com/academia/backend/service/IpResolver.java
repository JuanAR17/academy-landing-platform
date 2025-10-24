package com.academia.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IpResolver {

  private static final List<String> CANDIDATE_HEADERS = List.of(
      "CF-Connecting-IP",   // Cloudflare
      "X-Real-IP",
      "X-Forwarded-For",
      "X-Client-IP",
      "Forwarded",
      "X-Forwarded",
      "X-Cluster-Client-Ip"
  );

  public String resolve(HttpServletRequest req) {
    for (String h : CANDIDATE_HEADERS) {
      String v = req.getHeader(h);
      if (v == null || v.isBlank()) continue;

      // X-Forwarded-For: "client, proxy1, proxy2"
      if (h.equalsIgnoreCase("X-Forwarded-For")) {
        String ip = v.split(",")[0].trim();
        ip = sanitize(ip);
        if (!ip.isBlank()) return ip;
      }

      // Forwarded: for=1.2.3.4;proto=http;by=...
      if (h.equalsIgnoreCase("Forwarded")) {
        Matcher m = Pattern.compile("for=\"?\\[?([^;,\"]+)\\]?\"?").matcher(v);
        if (m.find()) {
          String ip = sanitize(m.group(1));
          if (!ip.isBlank()) return ip;
        }
      }

      // Otros headers simples
      String ip = sanitize(v);
      if (!ip.isBlank()) return ip;
    }

    // Fallback: remoto directo
    String ip = sanitize(req.getRemoteAddr());
    return ip.isBlank() ? "unknown" : ip;
  }

  private String sanitize(String ip) {
    if (ip == null) return "";
    // Quitar formato IPv6 con puerto [::1]:1234, zona, etc.
    ip = ip.trim();
    ip = ip.replaceAll("^\\[(.*)\\]$", "$1");  // [ipv6] -> ipv6
    ip = ip.replaceAll("%.*$", "");            // zona
    ip = ip.replaceAll(":\\d+$", "");          // puerto al final
    if (ip.startsWith("::ffff:")) ip = ip.substring(7); // IPv4-mapped
    return ip;
  }
}
