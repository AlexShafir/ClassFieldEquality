import com.alexshafir.classfieldequality.processor.AnnotationProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class TestProcessor {

    @Test
    fun test() {

        val originClass = SourceFile.kotlin("Origin.kt", """
           package com.test.pack1

           data class Origin(
                val param1:String,
                val param2:String
           )

        """)

        val targetClass = SourceFile.kotlin("Target.kt", """
            package com.test.pack2

            import com.alexshafir.classfieldequality.annotations.FieldEquality
            import com.test.pack1.Origin
    
            @FieldEquality(Origin::class)
            data class Target(
                 val param1:String,
                 val param2:String
            ) 

        """)

        val result = KotlinCompilation().apply {
            sources = listOf(originClass, targetClass)
            annotationProcessors = listOf(AnnotationProcessor())
            inheritClassPath = true
            verbose = false
        }.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        val output:String = result.sourcesGeneratedByAnnotationProcessor[0].bufferedReader().use { it.readText() }

        assertThat(output, equalTo("""
            package com.test.pack2

            import com.test.pack1.Origin

            public fun Target.mapToOrigin(): Origin = Origin(
            this.param1,
            this.param2
            )
            
        """.trimIndent()))

    }
}