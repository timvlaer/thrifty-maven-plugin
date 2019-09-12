package com.github.timvlaer.thrifty.plugin;

import com.github.timvlaer.thrifty.GlobalFlags;
import com.microsoft.thrifty.compiler.spi.TypeProcessor;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.List;

public class DelegatingTypeProcessor implements TypeProcessor {

  private static final List<TypeProcessor> typeProcessors = Arrays.asList(
      new TaggedUnionMethodTypeProcessor(),
      new BuilderMethodsTypeProcessor()
  );

  @Override
  public TypeSpec process(TypeSpec type) {
    if (GlobalFlags.enableConvenienceMethods) {
      for (TypeProcessor p : typeProcessors) {
        type = p.process(type);
      }
    }
    return type;
  }


}
