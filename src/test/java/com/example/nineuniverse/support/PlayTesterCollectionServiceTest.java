package com.example.nineuniverse.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.nineuniverse.domain.AppUser;
import com.example.nineuniverse.domain.CardDefinition;
import com.example.nineuniverse.repository.UserCollectionMapper;
import com.example.nineuniverse.security.AccountUserDetails;
import com.example.nineuniverse.service.CardCatalogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PlayTesterCollectionServiceTest {

	@Mock
	private UserCollectionMapper userCollectionMapper;

	@Mock
	private CardCatalogService cardCatalogService;

	@InjectMocks
	private PlayTesterCollectionService service;

	@Test
	void applyCollection_upsertsTwoForEachCatalogCard() {
		var c1 = new CardDefinition();
		c1.setId((short) 1);
		var c2 = new CardDefinition();
		c2.setId((short) 2);
		when(cardCatalogService.all()).thenReturn(List.of(c1, c2));

		service.applyCollection(42L);

		verify(userCollectionMapper).upsertExactQuantity(42L, (short) 1, 2);
		verify(userCollectionMapper).upsertExactQuantity(42L, (short) 2, 2);
	}

	@Test
	void syncOnLogin_skipsNonPlayTester() {
		var user = new AppUser();
		user.setId(1L);
		user.setUsername("alice");
		var auth = new UsernamePasswordAuthenticationToken(new AccountUserDetails(user), null);

		service.syncOnLogin(auth);

		verifyNoInteractions(userCollectionMapper);
	}

	@Test
	void syncIfPlayTester_appliesForPlayTesterSuffix() {
		var user = new AppUser();
		user.setId(7L);
		user.setUsername("bob_PlayTester");
		var auth = new UsernamePasswordAuthenticationToken(new AccountUserDetails(user), null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		var card = new CardDefinition();
		card.setId((short) 9);
		when(cardCatalogService.all()).thenReturn(List.of(card));

		service.syncIfPlayTester();

		verify(userCollectionMapper).upsertExactQuantity(eq(7L), eq((short) 9), eq(2));
		SecurityContextHolder.clearContext();
	}
}
