/*
 * Author : AdNovum Informatik AG
 */

package ch.adnovum.bootstrap.gradle.plugins

import groovy.transform.Canonical
import groovy.transform.PackageScope

@Canonical
class Patcher {
	def insertBefore
	def markerStart
	def markerEnd

	@PackageScope
	void patchStream(InputStream is, InputStream patch, OutputStream os) {
		def pattern = /$insertBefore/
		os.withPrintWriter { writer ->
			is.eachLine { line ->
				if (line =~ pattern) {
					writer.println markerStart
					writer << patch
					writer.println markerEnd
				}
				writer.println line
				writer.flush()
			}
		}
	}

	/*
	 * This method accepts Objects because it's not possible to define a common
	 * Supertype for the withInputStream and withOutputStream methods, since those
	 * are just regular extension methods of various classes.
	 */
	@PackageScope
	void patch(Object source, Object patch, Object target) {
		def baos = new ByteArrayOutputStream()
		source.withInputStream { is ->
			patch.withInputStream { ps ->
				patchStream(is, ps, baos)
			}
		}
		target.withOutputStream { os ->
			os << baos.toByteArray()
		}
	}
}
