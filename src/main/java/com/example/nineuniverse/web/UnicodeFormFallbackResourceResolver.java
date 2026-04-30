package com.example.nineuniverse.web;

import jakarta.servlet.http.HttpServletRequest;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * ZIP / JAR 内のファイル名の Unicode 正規形（NFC と NFD）が、リクエスト URL のパスと一致しない場合のフォールバック。
 * Windows でビルドした JAR を Linux 本番で配るときなどに、静的画像が 404 になるのを防ぐ。
 */
public final class UnicodeFormFallbackResourceResolver implements ResourceResolver {

	@Override
	@Nullable
	public Resource resolveResource(
			HttpServletRequest request,
			String requestPath,
			List<? extends Resource> locations,
			ResourceResolverChain chain) {
		for (String candidate : candidates(requestPath)) {
			Resource r = chain.resolveResource(request, candidate, locations);
			if (r != null && r.exists()) {
				return r;
			}
		}
		return null;
	}

	@Override
	@Nullable
	public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
		return chain.resolveUrlPath(resourcePath, locations);
	}

	private static Iterable<String> candidates(String requestPath) {
		Set<String> out = new LinkedHashSet<>();
		if (requestPath != null && !requestPath.isEmpty()) {
			out.add(requestPath);
			out.add(Normalizer.normalize(requestPath, Normalizer.Form.NFC));
			out.add(Normalizer.normalize(requestPath, Normalizer.Form.NFD));
		}
		return out;
	}
}
