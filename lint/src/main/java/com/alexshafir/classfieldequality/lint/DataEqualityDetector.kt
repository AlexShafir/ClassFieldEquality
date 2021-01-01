package com.alexshafir.classfieldequality.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassObjectAccessExpression
import com.alexshafir.classfieldequality.annotations.FieldEquality
import org.jetbrains.uast.UClass
import org.jetbrains.uast.kotlin.KotlinUClass

const val FEA_fqn = "com.alexshafir.classfieldequality.annotations.FieldEquality"

val issueEquality = Issue.create(
    "DataClassEquality",
    "Checks whether two data classes are equal",
    "Compares fields under the hood",
    Category.CORRECTNESS,
    5,
    Severity.FATAL,
    Implementation(DataEqualityDetector::class.java, Scope.JAVA_FILE_SCOPE)
)


class DataEqualityDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes() = listOf(
        UClass::class.java
    )

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                if(node !is KotlinUClass) return
                if(!node.hasAnnotation(FEA_fqn)) return

                val target:KotlinUClass = node

                val annot = node.getAnnotation(FEA_fqn)!!
                val expr = annot.findAttributeValue("value") as PsiClassObjectAccessExpression
                val origin = context.evaluator.getTypeClass(expr.type)!!

                // Actually compare two data classes

                // Compare amount of fields
                if(target.fields.size > origin.fields.size) {
                    context.report(
                        issue = issueEquality,
                        location = context.getNameLocation(target),
                        message = "Class ${target.name} (target) has ${target.fields.size - origin.fields.size} more fields than class ${origin.name} (origin)."
                    )

                    return
                } else if(target.fields.size < origin.fields.size) {
                    context.report(
                        issue = issueEquality,
                        location = context.getNameLocation(target),
                        message = "Class ${target.name} (target) has ${origin.fields.size - target.fields.size} less fields than class ${origin.name} (origin)"
                    )

                    return
                }

                // Compare fields
                val count = target.fields.size
                for(i in 0 until count) {
                    val f1 = target.fields[i]
                    val f2 = origin.fields[i]

                    // Compare field names
                    if(f1.name != f2.name) {
                        context.report(
                            issue = issueEquality,
                            location = context.getNameLocation(f1),
                            message = "Parameter's name should be ${f2.name}, as in class ${origin.name} (origin)."
                        )
                    }

                    // Compare field types
                    if(f1.type.canonicalText != f2.type.canonicalText) {
                        context.report(
                            issue = issueEquality,
                            location = context.getNameLocation(f1),
                            message = "Parameter's type should be ${f2.type.presentableText}, as in class ${origin.name} (origin)."
                        )
                    }
                }
            }

        }
    }


}