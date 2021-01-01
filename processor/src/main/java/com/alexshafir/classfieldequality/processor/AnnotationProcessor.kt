package com.alexshafir.classfieldequality.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.alexshafir.classfieldequality.annotations.FieldEquality
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter


// Aborts silently on check fail, as Lint should generate actual problem
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class AnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() = setOf(FieldEquality::class.qualifiedName)

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {

        roundEnv
            .getElementsAnnotatedWith(FieldEquality::class.java)
            .map { it as TypeElement }
            .forEach { processAnnotation(it) }

        return true
    }

    private fun getMyValue(annotation: FieldEquality): TypeMirror? {
        try {
            annotation.value // this should throw
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }
        return null // can this ever happen ??
    }

    private fun getClassName(element:TypeElement) = ClassName(
            processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString(),
            element.simpleName.toString()
        )


    private fun processAnnotation(target: TypeElement) {
        val typeUtils = processingEnv.typeUtils

        // Obtain annotation
        val annot = target.getAnnotation(FieldEquality::class.java) as FieldEquality

        // Obtain origin
        val origin = typeUtils.asElement(getMyValue(annot)!!) as TypeElement

        // Execute checks and fail silently if necessary
        val originFields = ElementFilter.fieldsIn(origin.enclosedElements)
        val targetFields = ElementFilter.fieldsIn(target.enclosedElements)

        // Check size
        if (originFields.size != targetFields.size) return

        // Check type and names
        val count = originFields.size
        for (i in 0 until count) {
            val f1 = originFields[i]
            val f2 = targetFields[i]

            if(f1.simpleName != f2.simpleName) return
            if(!(typeUtils.isSameType(f1.asType(), f2.asType()))) return

        }

        // Prepare statement
        var paramList = ""
        for(i in 0 until count) {
            paramList += "this.${originFields[i].simpleName}"
            if(i != count - 1) paramList += ",\n"
        }

        // Create function
        val funSpec = FunSpec.builder("mapToOrigin")
            .receiver(getClassName(target))
            .returns(getClassName(origin))
            .addStatement("return ${origin.simpleName}(\n$paramList\n)")

        // Create resulting file
        val className = target.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(target).qualifiedName.toString()

        val fileName = "${className}MapperExt"

        val fileBuilder = FileSpec.builder(pack, fileName)

        val file = fileBuilder.addFunction(funSpec.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir!!))
    }


}