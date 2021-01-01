package com.alexshafir.classfieldequality.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.TestFile
import com.android.tools.lint.checks.infrastructure.TestFile.KotlinTestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

// Lint Test does not interpret annotated files correctly: https://issuetracker.google.com/issues/119884022
// Lint Test does not resolve annotations in test code: https://issuetracker.google.com/issues/176280843
class TestDetector {

    val annotationSource: TestFile = KotlinTestFile.create(
        "com/alexshafir/classfieldequality/annotations/FieldEquality.kt",
        """
                        package com.alexshafir.classfieldequality.annotations

                        import kotlin.reflect.KClass

                        @Target(AnnotationTarget.CLASS)
                        @Retention(AnnotationRetention.RUNTIME)
                        annotation class FieldEquality(
                            val value: KClass<*>
                        ) 
                 """
        ).within("src").indented()

    val originClass = kt(
        """
                        package com.test.lint

                        data class Origin(
                            val param1:String,
                            val param2:String,
                        )
                 """
    ).indented()

    @Test
    fun testRealEquality() {
        lint()
            .files(
                annotationSource,
                originClass,

                KotlinTestFile.create(
                    "com/test/lint/Target.kt",
                    """
                        package com.test.lint
                        import com.alexshafir.classfieldequality.annotations.FieldEquality

                        @FieldEquality(Origin::class)
                        data class Target(
                            val param1:String,
                            val param2:String
                        )

                        """
                ).within("src").indented()

            )
            .issues(
                issueEquality
            )
            .run()
            .expectClean()

    }

    @Test
    fun testMoreParams() {
        lint()
            .files(
                annotationSource,
                originClass,

                KotlinTestFile.create(
                    "com/test/lint/Target.kt",
                    """
                        package com.test.lint
                        import com.alexshafir.classfieldequality.annotations.FieldEquality

                        @FieldEquality(Origin::class)
                        data class Target(
                            val param1:String,
                            val param2:String,
                            val param3: String
                        )

                        """
                ).within("src").indented()

            )
            .issues(
                issueEquality
            )
            .run()
            .expect(
                """
                src/com/test/lint/Target.kt:5: Error: Class Target (target) has 1 more fields than class Origin (origin). [DataClassEquality]
                data class Target(
                           ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )

    }

    @Test
    fun testDifferentFieldNames() {
        lint()
            .files(
                annotationSource,
                originClass,

                KotlinTestFile.create(
                    "com/test/lint/Target.kt",
                    """
                        package com.test.lint
                        import com.alexshafir.classfieldequality.annotations.FieldEquality

                        @FieldEquality(Origin::class)
                        data class Target(
                            val param1:String,
                            val param3:String
                        )

                        """
                ).within("src").indented()

            )
            .issues(
                issueEquality
            )
            .run()
            .expect(
                """
                src/com/test/lint/Target.kt:7: Error: Parameter's name should be param2, as in class Origin (origin). [DataClassEquality]
                    val param3:String
                        ~~~~~~
                1 errors, 0 warnings
                """.trimIndent())
    }

    @Test
    fun testDifferentFieldTypes() {
        lint()
            .files(
                annotationSource,
                originClass,

                KotlinTestFile.create(
                    "com/test/lint/Target.kt",
                    """
                        package com.test.lint
                        import com.alexshafir.classfieldequality.annotations.FieldEquality

                        @FieldEquality(Origin::class)
                        data class Target(
                            val param1:String,
                            val param2:Int
                        )

                        """
                ).within("src").indented()

            )
            .issues(
                issueEquality
            )
            .run()
            .expect(
                """
                    src/com/test/lint/Target.kt:7: Error: Parameter's type should be String, as in class Origin (origin). [DataClassEquality]
                        val param2:Int
                            ~~~~~~
                    1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun testMultipleProblems() {
        lint()
            .files(
                annotationSource,
                originClass,

                KotlinTestFile.create(
                    "com/test/lint/Target.kt",
                    """
                        package com.test.lint
                        import com.alexshafir.classfieldequality.annotations.FieldEquality

                        @FieldEquality(Origin::class)
                        data class Target(
                            val param1:String,
                            val param3:Int
                        )

                        """
                ).within("src").indented()

            )
            .issues(
                issueEquality
            )
            .run()
            .expect("""
                src/com/test/lint/Target.kt:7: Error: Parameter's name should be param2, as in class Origin (origin). [DataClassEquality]
                    val param3:Int
                        ~~~~~~
                src/com/test/lint/Target.kt:7: Error: Parameter's type should be String, as in class Origin (origin). [DataClassEquality]
                    val param3:Int
                        ~~~~~~
                2 errors, 0 warnings
            """.trimIndent())
    }
}