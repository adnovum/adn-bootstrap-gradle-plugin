/*
 * Author : AdNovum Informatik AG
 */

package ch.adnovum.bootstrap.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.InvalidUserDataException

class PatchFileTask extends DefaultTask {

	public static final String CONFIG_ERROR_BOOTSTRAP_SCRIPT_URL_MISSING = 'bootstrap.bootstrapScriptUrl is not defined!'

	@Input
	URL patch

	@Input
	URL initScript

	@OutputFile
	File wrapperFile

	@OutputFile
	File initFile

	@Input
	String markerStart = "### Start bootstrap patch ###"

	@Input
	String markerEnd = "### End bootstrap patch ###"

	@Input
	String insertBefore

	@TaskAction
	void actions() {
		placeScript()
		patch()
	}

	void placeScript() {
		def config = project.bootstrap as BootstrapPluginExtension

		if (config.bootstrapScriptUrl == null) {
				throw new InvalidUserDataException(CONFIG_ERROR_BOOTSTRAP_SCRIPT_URL_MISSING);
		}

		initScript.withInputStream { is ->
			initFile.text = is.text.denormalize()
				.replace('$$$DEFAULT_ADNDEVTOOLS_CACHE_DIR$$$', config.defaultAdnDevToolsCacheDir)
				.replace('$$$BOOTSTRAP_SCRIPT_URL$$$', config.bootstrapScriptUrl)
				.replace('$$$BOOTSTRAP_SCRIPT_DIR$$$', config.bootstrapScriptDir)
		}
		initFile.setExecutable(true, false);
	}

	void patch() {
		def patcher = new Patcher(insertBefore: insertBefore, markerStart: markerStart, markerEnd: markerEnd)
		patcher.patch(wrapperFile, patch, wrapperFile)
	}
}
