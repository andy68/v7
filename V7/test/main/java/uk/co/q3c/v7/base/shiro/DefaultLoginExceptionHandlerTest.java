package uk.co.q3c.v7.base.shiro;

import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import uk.co.q3c.v7.base.config.V7Ini;
import uk.co.q3c.v7.base.navigate.StandardPageKeys;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.view.std.LoginView;

import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;

import fixture.TestIniModule;

@RunWith(MycilaJunitRunner.class)
@GuiceContext({ TestIniModule.class })
public class DefaultLoginExceptionHandlerTest {

	// @Inject
	DefaultLoginExceptionHandler handler;

	@Mock
	LoginView loginView;

	@Mock
	V7Navigator navigator;

	@Inject
	V7Ini ini;

	UsernamePasswordToken token;

	@Before
	public void setup() {
		handler = new DefaultLoginExceptionHandler(navigator);
	}

	@Test
	public void accountLocked() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.accountLocked(loginView, token);
		// then
		verify(navigator).navigateTo(StandardPageKeys.Unlock_Account);

	}

	@Test
	public void unknownAccount() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.unknownAccount(loginView, token);
		// then
		verify(loginView).setStatusMessage(DefaultLoginExceptionHandler.invalidLogin);

	}

	@Test
	public void concurrentAccess() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.concurrentAccess(loginView, token);
		// then
		verify(loginView).setStatusMessage(DefaultLoginExceptionHandler.concurrent);

	}

	@Test
	public void disabledAccount() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.disabledAccount(loginView, token);
		// then
		verify(navigator).navigateTo(StandardPageKeys.Enable_Account);
	}

	@Test
	public void excessiveAttempts() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.excessiveAttempts(loginView, token);
		// then
		verify(navigator).navigateTo(StandardPageKeys.Reset_Account);
	}

	@Test
	public void expiredCredentials() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.expiredCredentials(loginView, token);
		// then
		verify(navigator).navigateTo(StandardPageKeys.Refresh_Account);
	}

	@Test
	public void incorrectCredentials() {
		// given
		token = new UsernamePasswordToken("fred", "password");
		// when
		handler.incorrectCredentials(loginView, token);
		// then
		verify(loginView).setStatusMessage(DefaultLoginExceptionHandler.invalidLogin);
	}
}
