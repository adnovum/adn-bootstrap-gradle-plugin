/*
 * Author : AdNovum Informatik AG
 */

package ch.adnovum.bootstrap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class BootstrapPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.extensions.create('bootstrap', BootstrapPluginExtension)
		def gradlewPatch = project.task('genericBootstrapGradlew', type: PatchFileTask) {
			initScript = this.class.classLoader.getResource 'initEnv'
			patch = this.class.classLoader.getResource 'generic-bootstrap.sh'
			insertBefore = '# Determine the Java command to use to start the JVM.'
			wrapperFile = project.rootProject.file('gradlew')
			initFile = project.rootProject.file('initEnv')
		}
		project.tasks.wrapper.finalizedBy gradlewPatch
	}
}

class BootstrapPluginExtension {
	String defaultAdnDevToolsCacheDir = '$HOME/.cache/adndevtools'
	String bootstrapScriptUrl
	String bootstrapScriptDir = '$ADNDEVTOOLS_CACHE'
}
