package org.eclipse.xtext.xbase.typesystem.references;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ArrayTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.CompoundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.FunctionTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.InnerFunctionTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.InnerTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.TypeReferenceVisitor;
import org.eclipse.xtext.xbase.typesystem.references.UnboundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.UnknownTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.WildcardTypeReference;

@SuppressWarnings("all")
public class LightweightTypeReferenceSerializer extends TypeReferenceVisitor {
  private ISourceAppender appender;
  
  public LightweightTypeReferenceSerializer(final ISourceAppender appender) {
    this.appender = appender;
  }
  
  @Override
  protected void doVisitAnyTypeReference(final AnyTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  @Override
  protected void doVisitArrayTypeReference(final ArrayTypeReference reference) {
    LightweightTypeReference _componentType = reference.getComponentType();
    _componentType.accept(this);
    this.appender.append("[]");
  }
  
  @Override
  protected void doVisitCompoundTypeReference(final CompoundTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  @Override
  protected void doVisitFunctionTypeReference(final FunctionTypeReference reference) {
    boolean _isJava = this.appender.isJava();
    if (_isJava) {
      this.doVisitParameterizedTypeReference(reference);
    } else {
      this.appender.append("(");
      List<LightweightTypeReference> _parameterTypes = reference.getParameterTypes();
      this.appendCommaSeparated(_parameterTypes);
      this.appender.append(")=>");
      LightweightTypeReference _returnType = reference.getReturnType();
      boolean _tripleEquals = (_returnType == null);
      if (_tripleEquals) {
        this.appender.append("void");
      } else {
        LightweightTypeReference _returnType_1 = reference.getReturnType();
        _returnType_1.accept(this);
      }
    }
  }
  
  @Override
  protected void doVisitInnerFunctionTypeReference(final InnerFunctionTypeReference reference) {
    boolean _isJava = this.appender.isJava();
    if (_isJava) {
      LightweightTypeReference _outer = reference.getOuter();
      _outer.accept(this);
      this.appender.append(".");
      JvmType _type = reference.getType();
      String _simpleName = _type.getSimpleName();
      this.appender.append(_simpleName);
      List<LightweightTypeReference> _typeArguments = reference.getTypeArguments();
      boolean _isEmpty = _typeArguments.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        this.appender.append("<");
        List<LightweightTypeReference> _typeArguments_1 = reference.getTypeArguments();
        this.appendCommaSeparated(_typeArguments_1);
        this.appender.append(">");
      }
    } else {
      this.appender.append("(");
      List<LightweightTypeReference> _parameterTypes = reference.getParameterTypes();
      this.appendCommaSeparated(_parameterTypes);
      this.appender.append(")=>");
      LightweightTypeReference _returnType = reference.getReturnType();
      boolean _tripleEquals = (_returnType == null);
      if (_tripleEquals) {
        this.appender.append("void");
      } else {
        LightweightTypeReference _returnType_1 = reference.getReturnType();
        _returnType_1.accept(this);
      }
    }
  }
  
  @Override
  protected void doVisitParameterizedTypeReference(final ParameterizedTypeReference reference) {
    boolean _isAnonymous = reference.isAnonymous();
    if (_isAnonymous) {
      LightweightTypeReference _namedType = reference.getNamedType();
      _namedType.accept(this);
    } else {
      JvmType _type = reference.getType();
      this.appender.append(_type);
      List<LightweightTypeReference> _typeArguments = reference.getTypeArguments();
      boolean _isEmpty = _typeArguments.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        this.appender.append("<");
        List<LightweightTypeReference> _typeArguments_1 = reference.getTypeArguments();
        this.appendCommaSeparated(_typeArguments_1);
        this.appender.append(">");
      }
    }
  }
  
  @Override
  protected void doVisitInnerTypeReference(final InnerTypeReference reference) {
    LightweightTypeReference _outer = reference.getOuter();
    _outer.accept(this);
    this.appender.append(".");
    JvmType _type = reference.getType();
    String _simpleName = _type.getSimpleName();
    this.appender.append(_simpleName);
    List<LightweightTypeReference> _typeArguments = reference.getTypeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.appender.append("<");
      List<LightweightTypeReference> _typeArguments_1 = reference.getTypeArguments();
      this.appendCommaSeparated(_typeArguments_1);
      this.appender.append(">");
    }
  }
  
  @Override
  protected void doVisitUnboundTypeReference(final UnboundTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  @Override
  protected void doVisitUnknownTypeReference(final UnknownTypeReference reference) {
    String _simpleName = reference.getSimpleName();
    this.appender.append(_simpleName);
  }
  
  @Override
  protected void doVisitWildcardTypeReference(final WildcardTypeReference reference) {
    this.appender.append("?");
    LightweightTypeReference _lowerBound = reference.getLowerBound();
    boolean _tripleNotEquals = (_lowerBound != null);
    if (_tripleNotEquals) {
      this.appender.append(" super ");
      LightweightTypeReference _lowerBound_1 = reference.getLowerBound();
      _lowerBound_1.accept(this);
    } else {
      List<LightweightTypeReference> _upperBounds = reference.getUpperBounds();
      final Function1<LightweightTypeReference, Boolean> _function = (LightweightTypeReference it) -> {
        String _identifier = it.getIdentifier();
        return Boolean.valueOf((!Objects.equal("java.lang.Object", _identifier)));
      };
      final Iterable<LightweightTypeReference> relevantUpperBounds = IterableExtensions.<LightweightTypeReference>filter(_upperBounds, _function);
      boolean _isEmpty = IterableExtensions.isEmpty(relevantUpperBounds);
      boolean _not = (!_isEmpty);
      if (_not) {
        this.appender.append(" extends ");
        boolean isFirst = true;
        for (final LightweightTypeReference upperBound : relevantUpperBounds) {
          {
            if ((!isFirst)) {
              this.appender.append(" & ");
            }
            isFirst = false;
            upperBound.accept(this);
          }
        }
      }
    }
  }
  
  protected void appendCommaSeparated(final List<LightweightTypeReference> references) {
    boolean isFirst = true;
    for (final LightweightTypeReference reference : references) {
      {
        if ((!isFirst)) {
          this.appender.append(", ");
        }
        isFirst = false;
        reference.accept(this);
      }
    }
  }
}
