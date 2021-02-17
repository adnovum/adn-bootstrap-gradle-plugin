/*
 * Author : AdNovum Informatik AG
 */

package ch.adnovum.bootstrap.gradle.plugins

import java.nio.file.Files

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Ignore
import ch.adnovum.bootstrap.gradle.plugins.PatchFileTask

class GradleWrapperPatchTest extends Specification {

	static final PATCH_LINE_COUNT = 3

	@Rule
	TemporaryFolder testProjectDir = new TemporaryFolder()

	@Rule
	TemporaryFolder refProjectDir = new TemporaryFolder()

	def buildFile
	def wrapperFile
	def initFile
	def patchedWrapperText
	def refWrapperText

	def referenceWrapperText() {
		refProjectDir.newFile('settings.gradle')
		refProjectDir.newFile('build.gradle')
		def refWrapper = new File(refProjectDir.root, "gradlew")
		Files.copy(
			this.getClass().getClassLoader().getResourceAsStream("gradlew"),
			refWrapper.toPath()
			)
		Files.copy(
			this.getClass().getClassLoader().getResourceAsStream("initEnv"),
			new File(refProjectDir.root, "initEnv").toPath()
			)

		GradleRunner.create().withProjectDir(refProjectDir.root).withArguments('wrapper').build()
		return refWrapper.text
	}

	def setup() {
		testProjectDir.newFile('settings.gradle')
		buildFile = testProjectDir.newFile('build.gradle')
		wrapperFile = new File(testProjectDir.root, "gradlew")
		initFile = new File(testProjectDir.root, "initEnv")
		patchedWrapperText = this.getClass()
			.getClassLoader()
			.getResourceAsStream("gradlew.patched").text.denormalize()
		refWrapperText = referenceWrapperText()
	}

	void "create new patched wrapper - bootstrapScriptUrl config param is required"() {
		given:
		buildFile << """
			plugins {
				id 'ch.adnovum.bootstrap'
			}
		"""

		when:
		def result = newBuild()
			.withArguments('wrapper')
			.buildAndFail()

		then:
		result.task(':genericBootstrapGradlew').outcome == TaskOutcome.FAILED
		result.output.contains(PatchFileTask.CONFIG_ERROR_BOOTSTRAP_SCRIPT_URL_MISSING)
	}

	void "create new patched wrapper with default settings"() {
		given:
		buildFile << """
			plugins {
				id 'ch.adnovum.bootstrap'
			}
			bootstrap {
				bootstrapScriptUrl = 'https://mycustomscript'
			}
		"""

		when:
		def result = newBuild()
			.withArguments('wrapper')
			.build()

		then:
		result.task(':genericBootstrapGradlew').outcome == TaskOutcome.SUCCESS
		wrapperFileIsCorrect()
		initFileIsCorrect('initEnvEtalonDefault')
	}

	void "create new patched wrapper with all custom properties"() {
		given:
		buildFile << """
			plugins {
				id 'ch.adnovum.bootstrap'
			}
			bootstrap {
				bootstrapScriptDir = '\$APP_HOME'
				defaultAdnDevToolsCacheDir = '.cache'
				bootstrapScriptUrl = 'https://mycustomscript'
			}
		"""

		when:
		def result = newBuild()
			.withArguments('wrapper')
			.build()

		then:
		result.task(':genericBootstrapGradlew').outcome == TaskOutcome.SUCCESS
		wrapperFileIsCorrect()
		initFileIsCorrect('initEnvEtalonCustom')
	}

	void "added patch lines are correct"() {
		given:
		buildFile << """
			plugins {
				id 'ch.adnovum.bootstrap'
			}
			bootstrap {
				bootstrapScriptUrl = 'https://mycustomscript'
			}
		"""
		Files.copy(
			this.getClass().getClassLoader().getResourceAsStream("gradlew"),
			new File(testProjectDir.root, "gradlew").toPath()
			)
		when:
		def result = newBuild()
			.withArguments('genericBootstrapGradlew')
			.build()

		then:
		result.task(':genericBootstrapGradlew').outcome == TaskOutcome.SUCCESS
		wrapperFile.exists()
		wrapperFile.canRead()
		wrapperFile.text == patchedWrapperText
	}

	def newBuild() {
		GradleRunner.create()
			.withProjectDir(testProjectDir.root)
			.forwardOutput()
			.withPluginClasspath()
	}

	void initFileIsCorrect(String expectedContentsFile) {
		assert initFile.exists()
		assert initFile.canRead()
		assert initFile.canExecute()
		assert initFile.text == this.getClass()
			.getClassLoader()
			.getResourceAsStream(expectedContentsFile).text.denormalize()

	}

	void wrapperFileIsCorrect() {
		assert wrapperFile.exists()
		assert wrapperFile.canRead()
		assert wrapperFile.canExecute()
		assert wrapperFile.readLines().size == refWrapperText.readLines().size + PATCH_LINE_COUNT
	}
}
