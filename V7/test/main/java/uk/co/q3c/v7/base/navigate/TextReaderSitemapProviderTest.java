/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base.navigate;

import static org.fest.assertions.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Fail;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.co.q3c.v7.base.view.std.PublicHomeView;
import uk.co.q3c.v7.base.view.std.PublicView;
import uk.co.q3c.v7.base.view.std.SecureHomeView;
import uk.co.q3c.v7.base.view.std.SystemAccountView;
import uk.co.q3c.v7.base.view.testviews.subview.MoneyInOutView;
import uk.co.q3c.v7.base.view.testviews.subview.NotV7View;
import uk.co.q3c.v7.base.view.testviews.subview.TransferView;
import uk.co.q3c.v7.i18n.TestLabelKeys;

import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;

import fixture.testviews2.AlternateAccountView;
import fixture.testviews2.LogoutView;
import fixture.testviews2.OptionsView;
import fixture.testviews2.TestLoginView;

@RunWith(MycilaJunitRunner.class)
@GuiceContext({})
public class TextReaderSitemapProviderTest {

	private static File propDir;
	private File propFile;
	private static File modifiedFile;
	private List<String> lines;
	@Inject
	TextReaderSitemapProvider reader;

	@BeforeClass
	public static void beforeClass() {
		propDir = new File("test/main/java/uk/co/q3c/v7/base/navigate");
		File modDir = new File(System.getProperty("user.home"));
		modifiedFile = new File(modDir, "temp/sitemap.properties");
	}

	@Before
	public void setup() throws IOException {
		loadMasterFile();
	}

	/**
	 * Map does not define every page, some are automatically added standard pages
	 * 
	 * @throws IOException
	 */
	@Test
	public void parse_partialMap() throws IOException {

		// given
		String propFileName = "sitemap_partial.properties";
		propFile = new File(propDir, propFileName);
		DateTime start = DateTime.now();
		// when
		assertThat(propFile.exists()).isTrue();
		reader.parse(propFile);
		// then
		assertThat(reader.getSitemap()).isNotNull();
		assertThat(reader.getCommentLines()).isEqualTo(26);
		assertThat(reader.getBlankLines()).isEqualTo(9);
		assertThat(reader.getSections()).containsOnly("viewPackages", "options", "map", "standardPages", "redirects");
		assertThat(reader.isLabelClassMissing()).isFalse();
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.getLabelKeys()).isEqualTo("uk.co.q3c.v7.i18n.TestLabelKeys");
		assertThat(reader.isAppendView()).isTrue();
		assertThat(reader.getLabelKeysClass()).isEqualTo(TestLabelKeys.class);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.redirectEntries()).containsOnly(":public/home", "public:public/home", "secure:secure/home");
		assertThat(reader.getMissingEnums()).isEmpty();

		assertThat(reader.getSitemap().getNodeCount()).isEqualTo(14);
		assertThat(reader.missingSections().size()).isEqualTo(0);
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();

		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();

		Sitemap tree = reader.getSitemap();
		List<SitemapNode> roots = tree.getRoots();
		assertThat(roots.size()).isEqualTo(2);

		System.out.println(tree.toString());

		Collection<SitemapNode> nodes = reader.getSitemap().getEntries();
		for (SitemapNode node : nodes) {
			validateNode(tree, node);
		}

		assertThat(reader.getStartTime()).isNotNull();
		assertThat(reader.getEndTime()).isNotNull();
		assertThat(reader.getStartTime().getMillis()).isGreaterThanOrEqualTo(start.getMillis());
		assertThat(reader.getEndTime().isAfter(reader.getStartTime())).isTrue();

		assertThat(reader.getReport()).isNotNull();
		assertThat(reader.getReport().toString()).isNotEmpty();

		for (StandardPageKeys spk : StandardPageKeys.values()) {
			assertThat(reader.standardPageUrl(spk)).isNotNull();
		}
		assertThat(reader.getSitemap().hasErrors()).isFalse();
		System.out.println(reader.getReport().toString());

		System.out.println(reader.getSitemap());
	}

	/**
	 * Map does defines every page, and therefore has predictable page ordering
	 * 
	 * @throws IOException
	 */
	@Test
	public void parse_fullMap() throws IOException {

		// given
		String propFileName = "sitemap_full.properties";
		propFile = new File(propDir, propFileName);
		DateTime start = DateTime.now();
		// when
		assertThat(propFile.exists()).isTrue();
		System.out.println(propFile.getAbsolutePath());
		reader.parse(propFile);
		// then
		assertThat(reader.getSitemap()).isNotNull();
		assertThat(reader.getCommentLines()).isEqualTo(21);
		assertThat(reader.getBlankLines()).isEqualTo(9);
		assertThat(reader.getSections()).containsOnly("viewPackages", "options", "map", "redirects");
		assertThat(reader.isLabelClassMissing()).isFalse();
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.getLabelKeys()).isEqualTo("uk.co.q3c.v7.i18n.TestLabelKeys");
		assertThat(reader.isAppendView()).isTrue();
		assertThat(reader.getLabelKeysClass()).isEqualTo(TestLabelKeys.class);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews",
				"uk.co.q3c.v7.base.view.std");
		assertThat(reader.redirectEntries()).containsOnly(":public/home", "public:public/home", "secure:secure/home");
		assertThat(reader.getMissingEnums()).isEmpty();

		// assertThat(reader.getSitemap().getNodeCount()).isEqualTo(14);
		assertThat(reader.missingSections().size()).isEqualTo(0);
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();

		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();

		Sitemap tree = reader.getSitemap();
		List<SitemapNode> roots = tree.getRoots();
		assertThat(roots.size()).isEqualTo(2);

		Collection<SitemapNode> nodes = reader.getSitemap().getEntries();
		for (SitemapNode node : nodes) {
			validateNode(tree, node);
		}

		assertThat(reader.getStartTime()).isNotNull();
		assertThat(reader.getEndTime()).isNotNull();
		assertThat(reader.getStartTime().getMillis()).isGreaterThanOrEqualTo(start.getMillis());
		assertThat(reader.getEndTime().isAfter(reader.getStartTime())).isTrue();

		assertThat(reader.getReport()).isNotNull();
		assertThat(reader.getReport().toString()).isNotEmpty();

		for (StandardPageKeys spk : StandardPageKeys.values()) {
			assertThat(reader.standardPageUrl(spk)).isNotNull();
		}
		assertThat(reader.getSitemap().hasErrors()).isFalse();

		outputModifiedFile();
		outputSitemap();
	}

	@Test
	public void standardPagesNotSpecified() {

		// given

		// when

		// then
		assertThat(false).isEqualTo(true);

	}

	@Test
	public void sectionMissingClosingBracket() throws IOException {

		// given
		// given
		substitute("[viewPackages]", "[viewPackages");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly("viewPackages");

		assertThat(reader.getPagesDefined()).isEqualTo(0);
		assertThat(reader.getViewPackages()).isNull();
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();
		System.out.println(reader.getReport());
	}

	@Test
	public void invalidPropertyName() throws IOException {

		insertAfter("labelKeys=uk.co.q3c.v7.i18n.TestLabelKeys", "randomProperty=23");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isFalse();

		System.out.println(reader.getReport());

	}

	@Test
	public void invalidSectionName() throws IOException {

		// given
		substitute("[options]", "[option]");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.missingSections()).containsOnly("options");

		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.getPagesDefined()).isEqualTo(0);
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();

		System.out.println(reader.getReport());
	}

	/**
	 * Does not implement i18N
	 * 
	 * @throws IOException
	 */
	@Test
	public void invalidLabelKeysClass_no_i18N() throws IOException {

		// given
		substitute("labelKeys=uk.co.q3c.v7.i18n.TestLabelKeys", "labelKeys=uk.co.q3c.v7.i18n.TestLabelKeys_Invalid");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.isLabelClassMissing()).isFalse();
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isTrue();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).contains("MoneyInOut", "Transfers", "Opt");
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();
		System.out.println(reader.getReport());
	}

	/**
	 * Does not exist
	 * 
	 * @throws IOException
	 */
	@Test
	public void invalidLabelKeysClass_does_not_exist() throws IOException {
		// given
		substitute("labelKeys=uk.co.q3c.v7.i18n.TestLabelKeys", "labelKeys=uk.co.q3c.v7.i18n.TestLabelKeys2");
		prepFile();
		outputModifiedFile();
		// when
		reader.parse(modifiedFile);

		// then
		assertThat(reader.isLabelClassMissing()).isFalse();
		assertThat(reader.isLabelClassNonExistent()).isTrue();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(17);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		// multiple failures where an attempt is made to identify from segment as well
		assertThat(reader.getMissingEnums()).containsOnly("MoneyInOut", "Money_in_out", "Opt", "Options", "Transfers",
				"Secure", "Public", "Home");
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();

		System.out.println(reader.getReport());
		outputSitemap();
	}

	/**
	 * An invalid view is defined in standard pages, but redefined in map correctly
	 * 
	 * @throws IOException
	 */
	@Test
	public void standardpage_view_overruled() throws IOException {

		substitute("secureHome     = secure/home                  : SecureHome",
				"secureHome     = secure/home                  : SecurelyHome");
		prepFile();
		outputModifiedFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isFalse();
		System.out.println(reader.getReport());
	}

	@Test
	public void viewNotFound() throws IOException {

		substitute("--home          : PublicHome", "--home          : PubliclyHome");
		prepFile();
		outputModifiedFile();
		// when
		reader.parse(modifiedFile);
		// then

		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly("PubliclyHomeView");
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();
		System.out.println(reader.getReport());

	}

	@Test
	public void viewNotV7View() throws IOException {

		substitute("--money-in-out  : subview.MoneyInOut      ~ MoneyInOut",
				"--money-in-out : subview.NotV7 ~ MoneyInOut");
		prepFile();
		// when
		reader.parse(modifiedFile);

		// then
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly(NotV7View.class.getName());
		assertThat(reader.getUndeclaredViewClasses()).containsOnly("subview.NotV7View");
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();
		System.out.println(reader.getReport());

	}

	/**
	 * Tries to go out of structure by double indenting from previous
	 * 
	 * @throws IOException
	 */
	@Test
	public void mapIndentTooGreat() throws IOException {

		substitute("--transfers     : subview.Transfer", "----transfers     : subview.Transfer");
		prepFile();
		// when
		reader.parse(modifiedFile);

		// then
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(14);
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly();
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly("transfers");
		assertThat(reader.getSitemap().hasErrors()).isFalse();

		System.out.println(reader.getReport());
		System.out.println(reader.getSitemap().toString());
	}

	/**
	 * Try to call report before parsing anything
	 */
	@Test(expected = SiteMapException.class)
	public void reportBeforeParse() {

		// given

		// when
		reader.getReport();
		// then

	}

	@Test
	public void standardPageMissing() throws IOException {

		// given
		substitute("resetAccount   = public/reset-account", "resetAccount   = ");
		substitute("requestAccount = public/request-account", null);
		prepFile();
		// when
		reader.parse(modifiedFile);

		// then
		assertThat(reader.isLabelClassNonExistent()).isFalse();
		assertThat(reader.isLabelClassNotI18N()).isFalse();
		assertThat(reader.missingSections()).containsOnly();

		assertThat(reader.getPagesDefined()).isEqualTo(12); // Two standard pages missing
		assertThat(reader.getViewPackages()).containsOnly("fixture.testviews2", "uk.co.q3c.v7.base.view.testviews");
		assertThat(reader.getMissingPages()).containsOnly("requestAccount");
		assertThat(reader.getPropertyErrors()).containsOnly();
		assertThat(reader.getMissingEnums()).containsOnly();
		assertThat(reader.getInvalidViewClasses()).containsOnly();
		assertThat(reader.getUndeclaredViewClasses()).containsOnly();
		assertThat(reader.getIndentationErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isTrue();

		System.out.println(reader.getReport());
	}

	@Test
	public void standardPageEmpty() throws IOException {

		// given
		substitute("publicHome     = public/home                  : PublicHome",
				"publicHome     =                 : PublicHome");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then
		assertThat(reader.getSitemap().standardPageURI(StandardPageKeys.Public_Home)).isEqualTo("");

	}

	@Test
	public void redirectTargetNotADefinedPage() throws IOException {

		// given
		substitute("public : public/home", "public : wiggly/home");
		prepFile();
		// when
		reader.parse(modifiedFile);
		// then
		assertThat(reader.getRedirectErrors()).containsOnly(
				"'wiggly/home' cannot be a redirect target, it has not been defined as a page");
		assertThat(reader.getSitemap().hasErrors()).isTrue();

	}

	@Test
	public void redirectTargetEmptyButValid() throws IOException {

		// given
		substitute("       : public/home", "wiggly  :   "); // redirect
		substitute("publicHome     = public/home                  : PublicHome",
				"publicHome     =                 : PublicHome");
		insertAfter("--options                                 ~ Opt", "-public");
		insertAfter("-public", "--home   :  PublicHome");
		prepFile();
		// when
		reader.parse(modifiedFile);
		System.out.println(reader.getSitemap().toString());
		// then

		assertThat(reader.getSitemap().urls()).contains("");
		assertThat(reader.getRedirectErrors()).containsOnly();
		assertThat(reader.getSitemap().hasErrors()).isFalse();

	}

	@Test
	public void redirectTargetLoop() throws IOException {

		// given
		insertAfter("secure : secure/home", "public/home : public");
		prepFile();
		// when
		reader.parse(modifiedFile);

		// then
		assertThat(reader.getRedirectErrors()).contains(
				"'public/home' cannot be both a redirect source and redirect target");
		assertThat(reader.getRedirectErrors())
				.contains("'public' cannot be both a redirect source and redirect target");
		assertThat(reader.getSitemap().hasErrors()).isTrue();

	}

	private void validateNode(Sitemap tree, SitemapNode node) {
		String url = tree.url(node);
		System.out.println("validating node for " + url);
		switch (url) {

		case "public":
			assertThat(tree.getChildCount(node)).isEqualTo(4);
			assertThat(node.getUrlSegment()).isEqualTo("public");
			assertThat(node.getViewClass()).isEqualTo(PublicView.class);
			assertThat(node.getLabelKey()).isNotNull();
			break;

		case "public/home": {
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("home");
			assertThat(node.getViewClass()).isEqualTo(PublicHomeView.class);
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Public_Home);

			break;
		}

		case "public/login": {
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("login");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Login);
			assertThat(node.getViewClass()).isEqualTo(TestLoginView.class);
			break;
		}

		case "public/logout": {
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("logout");
			assertThat(node.getViewClass()).isEqualTo(LogoutView.class);
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Logout);
			break;
		}

		case "public/system-account/enable-account":
			assertThat(node.getUrlSegment()).isEqualTo("enable-account");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Enable_Account);
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getViewClass()).isEqualTo(AlternateAccountView.class);
			break;
		case "public/system-account/request-account":
			assertThat(node.getUrlSegment()).isEqualTo("request-account");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Request_Account);
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getViewClass()).isEqualTo(AlternateAccountView.class);
			break;
		case "public/system-account/refresh-account":
			assertThat(node.getUrlSegment()).isEqualTo("refresh-account");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Refresh_Account);
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getViewClass()).isEqualTo(AlternateAccountView.class);
			break;
		case "public/system-account/unlock-account":
			assertThat(node.getUrlSegment()).isEqualTo("unlock-account");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Unlock_Account);
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getViewClass()).isEqualTo(AlternateAccountView.class);
			break;
		case "public/system-account/reset-account": {
			assertThat(node.getUrlSegment()).isEqualTo("reset-account");
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Reset_Account);
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getViewClass()).isEqualTo(AlternateAccountView.class);
			break;
		}

		case "secure":
			assertThat(tree.getChildCount(node)).isEqualTo(4);
			assertThat(node.getUrlSegment()).isEqualTo("secure");
			assertThat(node.getViewClass()).isEqualTo(null);
			assertThat(node.getLabelKey()).isNotNull();
			break;

		case "secure/home":
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("home");
			assertThat(node.getViewClass()).isEqualTo(SecureHomeView.class);
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.Secure_Home);
			break;

		case "secure/transfers":
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("transfers");
			assertThat(node.getViewClass()).isEqualTo(TransferView.class);
			assertThat(node.getLabelKey()).isEqualTo(TestLabelKeys.Transfers);
			break;

		case "secure/money-in-out":
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("money-in-out");
			assertThat(node.getViewClass()).isEqualTo(MoneyInOutView.class);
			assertThat(node.getLabelKey()).isEqualTo(TestLabelKeys.MoneyInOut);
			break;

		case "secure/options":
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("options");
			assertThat(node.getViewClass()).isEqualTo(OptionsView.class);
			assertThat(node.getLabelKey()).isEqualTo(TestLabelKeys.Opt);
			break;

		case "public/system-account":
			assertThat(tree.getChildCount(node)).isEqualTo(0);
			assertThat(node.getUrlSegment()).isEqualTo("system-account");
			assertThat(node.getViewClass()).isEqualTo(SystemAccountView.class);
			assertThat(node.getLabelKey()).isEqualTo(StandardPageKeys.System_Account);
			break;

		default:
			Fail.fail("unexpected url: '" + url + "'");
		}
	}

	@SuppressWarnings("unchecked")
	private void loadMasterFile() throws IOException {
		propFile = new File(propDir, "sitemap_partial.properties");
		lines = FileUtils.readLines(propFile);
	}

	private void prepFile() throws IOException {
		FileUtils.writeLines(modifiedFile, lines);
	}

	private void substitute(String original, String replacement) {
		int index = lines.indexOf(original);
		if (index >= 0) {
			lines.remove(index);
		} else {
			throw new RuntimeException("Subsitution failed in test setup, " + original + " was not found");
		}
		if (replacement != null) {
			lines.add(index, replacement);
		}
	}

	private void insertAfter(String reference, String insertion) {
		int index = lines.indexOf(reference);
		lines.add(index + 1, insertion);
	}

	private void outputModifiedFile() {
		System.out.println("/n===================== input file =================== ");
		for (String line : lines) {
			System.out.println(line);
		}
	}

	private void outputSitemap() {
		System.out.println("/n===================== sitemap =================== ");
		System.out.println(reader.getSitemap().toString());
	}

}
