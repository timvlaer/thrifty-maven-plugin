package com.github.timvlaer.thrifty.plugin;

import com.microsoft.thrifty.compiler.spi.TypeProcessor;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class BuilderMethodsTypeProcessor implements TypeProcessor {

  public static final String NEW_BUILDER_METHOD_NAME = "builder";
  public static final String TO_BUILDER_METHOD_NAME = "toBuilder";

  private static final String BUILDER_CLASS_NAME = "Builder";

  @Override
  public TypeSpec process(TypeSpec type) {
      if (hasBuilderClass(type)) {
        return type.toBuilder()
            .addMethod(createNewBuilderMethod())
            .addMethod(createNewBuilderMethodWithParameter(type))
            .addMethod(createToBuilderMethod())
            .build();
      }
    return type;
  }

  private MethodSpec createNewBuilderMethod() {
    ClassName builder = ClassName.bestGuess("Builder");

    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    codeBlockBuilder.addStatement("return new $T()", builder);

    return MethodSpec.methodBuilder(NEW_BUILDER_METHOD_NAME)
        .returns(builder)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addCode(codeBlockBuilder.build())
        .build();
  }

  private MethodSpec createNewBuilderMethodWithParameter(TypeSpec type) {
    ClassName builder = ClassName.bestGuess("Builder");

    ParameterSpec param = ParameterSpec.builder(ClassName.bestGuess(type.name), "prototype").build();

    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    codeBlockBuilder.addStatement("return new $T($N)", builder, param.name);

    return MethodSpec.methodBuilder(NEW_BUILDER_METHOD_NAME)
        .addParameter(param)
        .returns(builder)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addCode(codeBlockBuilder.build())
        .build();
  }

  private MethodSpec createToBuilderMethod() {
    ClassName builder = ClassName.bestGuess("Builder");

    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    codeBlockBuilder.addStatement("return new $T($L)", builder, "this");

    return MethodSpec.methodBuilder(TO_BUILDER_METHOD_NAME)
        .returns(builder)
        .addModifiers(Modifier.PUBLIC)
        .addCode(codeBlockBuilder.build())
        .build();
  }

  private Boolean hasBuilderClass(TypeSpec type) {
    return type.typeSpecs.stream().anyMatch(e -> BUILDER_CLASS_NAME.equals(e.name));
  }

}
