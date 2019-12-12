package com.github.timvlaer.thrifty.plugin;

import com.github.timvlaer.thrifty.GlobalFlags;
import com.microsoft.thrifty.compiler.spi.TypeProcessor;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class BuilderGetterMethodsTypeProcessor implements TypeProcessor {

  private static final String BUILDER_CLASS_NAME = "Builder";

  @Override
  public TypeSpec process(TypeSpec type) {
    if (GlobalFlags.generateGettersInBuilders) {
      Optional<TypeSpec> builderClassOpt = getBuilderClass(type);
      if (builderClassOpt.isPresent()) {
        TypeSpec builderClassSpec = builderClassOpt.get();

        TypeSpec.Builder builder = createTypeBuilderWithoutBuilderClass(type);
        builder.addType(
            builderClassSpec.toBuilder()
                .addMethods(builderClassSpec.fieldSpecs.stream()
                    .map(this::addGetter)
                    .collect(toList()))
                .build()
        );
        return builder.build();
      }
    }
    return type;
  }

  @NotNull
  private TypeSpec.Builder createTypeBuilderWithoutBuilderClass(TypeSpec type) {
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

  private MethodSpec addGetter(FieldSpec f) {
    return MethodSpec.methodBuilder(f.name)
        .addModifiers(Modifier.PUBLIC)
        .returns(f.type)
        .addStatement("return $N", f.name)
        .build();
  }

  private Optional<TypeSpec> getBuilderClass(TypeSpec type) {
    return type.typeSpecs.stream().filter(e -> BUILDER_CLASS_NAME.equals(e.name)).findFirst();
  }

}
