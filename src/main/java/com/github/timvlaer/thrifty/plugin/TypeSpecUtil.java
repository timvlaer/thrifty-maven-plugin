package com.github.timvlaer.thrifty.plugin;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypeSpecUtil {
  public static final String BUILDER_CLASS_NAME = "Builder";

  public static Optional<TypeSpec> getBuilderClass(TypeSpec type) {
    return type.typeSpecs.stream().filter(e -> BUILDER_CLASS_NAME.equals(e.name)).findFirst();
  }

  public static TypeSpec.Builder toBuilderWithoutBuilderClass(TypeSpec type) {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type.name);
    builder.addJavadoc(type.javadoc);
    builder.addAnnotations(type.annotations);
    builder.addModifiers(type.modifiers.toArray(new Modifier[]{}));
    builder.addTypeVariables(type.typeVariables);
    builder.superclass(type.superclass);
    builder.addSuperinterfaces(type.superinterfaces);
    type.enumConstants.forEach(builder::addEnumConstant);
    builder.addFields(type.fieldSpecs);
    builder.addMethods(type.methodSpecs);
    builder.addTypes(type.typeSpecs.stream().filter(e -> !BUILDER_CLASS_NAME.equals(e.name)).collect(Collectors.toList()));
    builder.addInitializerBlock(type.initializerBlock);
    builder.addStaticBlock(type.staticBlock);
    return builder;
  }
}
