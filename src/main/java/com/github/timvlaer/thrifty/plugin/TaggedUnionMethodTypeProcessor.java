package com.github.timvlaer.thrifty.plugin;

import com.microsoft.thrifty.compiler.spi.TypeProcessor;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class TaggedUnionMethodTypeProcessor implements TypeProcessor {

  public static final String TAG_METHOD_NAME = "tag";
  public static final String VALUE_METHOD_NAME = "value";

  private static final String BUILDER_CLASS_NAME = "Builder";
  public static final String BUILD_METHOD_NAME = "build";

  @Override
  public TypeSpec process(TypeSpec type) {
    if (wasThriftUnion(type)) {
      TypeSpec.Builder builder = type.toBuilder();
      builder.addMethod(createTagMethod(type));
      builder.addMethod(createValueMethod(type));
      builder.addMethods(createStaticFactoryMethods(type));
      return builder.build();
    }

    return type;
  }

  private MethodSpec createTagMethod(TypeSpec type) {
    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    getUnionFieldSpecs(type)
        .forEach(
            fieldSpec ->
                codeBlockBuilder.addStatement(
                    "if ($N != null) return $S", fieldSpec.name, fieldSpec.name));
    codeBlockBuilder.addStatement(
        "throw new $T($S)", IllegalStateException.class, "Union type should have one value!");

    return createPublicMethod(TAG_METHOD_NAME, String.class, codeBlockBuilder.build());
  }

  private MethodSpec createValueMethod(TypeSpec type) {
    CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
    getUnionFieldSpecs(type)
        .forEach(
            fieldSpec ->
                codeBlockBuilder.addStatement(
                    "if ($N != null) return $N", fieldSpec.name, fieldSpec.name));
    codeBlockBuilder.addStatement(
        "throw new $T($S)", IllegalStateException.class, "Union type should have one value!");

    return createPublicMethod(VALUE_METHOD_NAME, Object.class, codeBlockBuilder.build());
  }

  private List<FieldSpec> getUnionFieldSpecs(TypeSpec type) {
    return type.fieldSpecs.stream()
        .filter(f -> !f.modifiers.contains(Modifier.STATIC))
        .collect(Collectors.toList());
  }

  private MethodSpec createPublicMethod(String methodName, Class returnType, CodeBlock codeBlock) {
    return MethodSpec.methodBuilder(methodName)
        .returns(returnType)
        .addModifiers(Modifier.PUBLIC)
        .addCode(codeBlock)
        .build();
  }

  private List<MethodSpec> createStaticFactoryMethods(TypeSpec type) {
    return getUnionFieldSpecs(type).stream()
        .map(
            f ->
                MethodSpec.methodBuilder(f.name)
                    .returns(ClassName.bestGuess(type.name))
                    .addParameter(f.type, f.name)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("return new Builder().$L($L).build()", f.name, f.name)
                            .build())
                    .build())
        .collect(Collectors.toList());
  }

  private Boolean wasThriftUnion(TypeSpec type) {
    return type.superinterfaces.contains(ClassName.get(com.microsoft.thrifty.Struct.class))
        && type.typeSpecs.stream()
            .filter(e -> BUILDER_CLASS_NAME.equals(e.name))
            .findFirst()
            .flatMap(
                t ->
                    t.methodSpecs.stream()
                        .filter(m -> BUILD_METHOD_NAME.equals(m.name))
                        .findFirst())
            .map(m -> m.code.toString().contains("Invalid union;"))
            .orElse(false);
  }
}
