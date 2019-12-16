package com.github.timvlaer.thrifty.plugin;

import com.github.timvlaer.thrifty.GlobalFlags;
import com.microsoft.thrifty.compiler.spi.TypeProcessor;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class BuilderGetterMethodsTypeProcessor implements TypeProcessor {

  @Override
  public TypeSpec process(TypeSpec type) {
    if (GlobalFlags.generateGettersInBuilders) {
      Optional<TypeSpec> builderClassOpt = TypeSpecUtil.getBuilderClass(type);
      if (builderClassOpt.isPresent()) {
        TypeSpec builderClassSpec = builderClassOpt.get();

        TypeSpec.Builder builder = TypeSpecUtil.toBuilderWithoutBuilderClass(type);
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

  private MethodSpec addGetter(FieldSpec f) {
    return MethodSpec.methodBuilder(f.name)
        .addModifiers(Modifier.PUBLIC)
        .returns(f.type)
        .addStatement("return $N", f.name)
        .build();
  }

}
