/*
 * Author : AdNovum Informatik AG
 */

package ch.adnovum.bootstrap.gradle.plugins

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class PatcherTest extends Specification {

	@Rule
	TemporaryFolder temporaryFolder

	def targetFile
	def patchFile
	def patcher

	def setup() {
		targetFile = temporaryFolder.newFile('target')
		patchFile = temporaryFolder.newFile('patch')
		patcher = new Patcher(insertBefore: '# insert before this', markerStart: '# start', markerEnd: '# end')
	}

	def 'should patch file'() {
		given:
		targetFile.text =	'''\
							|this is a text
							|before the patch
							|# insert before this
							|after the patch
							|'''.stripMargin().denormalize()
		patchFile.text =	'''\
							|hello
							|world
							|'''.stripMargin().denormalize()
		def	expected =		'''\
							|this is a text
							|before the patch
							|# start
							|hello
							|world
							|# end
							|# insert before this
							|after the patch
							|'''.stripMargin().denormalize()
		when:
		patcher.patch(targetFile, patchFile, targetFile)
		def actual = targetFile.text
		then:
		actual == expected
	}

	def 'should patch stream' () {
		given:
		def source =	'''\
						|this is a text
						|before the patch
						|# insert before this
						|after the patch
						|'''.stripMargin().denormalize()
		def patch =		'''\
						|hello
						|world
						|'''.stripMargin().denormalize()
		def	expected =	'''\
						|this is a text
						|before the patch
						|# start
						|hello
						|world
						|# end
						|# insert before this
						|after the patch
						|'''.stripMargin().denormalize()
		def sourceStream = new ByteArrayInputStream(source.bytes)
		def patchStream = new ByteArrayInputStream(patch.bytes)
		def targetStream = new ByteArrayOutputStream()
		when:
		patcher.patchStream(sourceStream, patchStream, targetStream)
		def actual = new String(targetStream.toByteArray())
		then:
		actual == expected
	}
}
