package org.infinispan.objectfilter.impl.syntax;

import org.infinispan.objectfilter.impl.util.DateHelper;

import java.util.Date;
import java.util.Map;

/**
 * A constant comparable value, to be used as right or left side in a comparison expression.
 *
 * @author anistor@redhat.com
 * @since 7.0
 */
public final class ConstantValueExpr implements ValueExpr {

   public static final class ParamPlaceholder implements Comparable {

      private final String name;

      public ParamPlaceholder(String name) {
         if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
         }
         this.name = name;
      }

      public String getName() {
         return name;
      }

      @Override
      public String toString() {
         return ":" + getName();
      }

      @Override
      public int compareTo(Object o) {
         return 0;  //todo [anistor] hack!
      }

      @Override
      public int hashCode() {
         return name.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj != null && obj.getClass() == ParamPlaceholder.class && name.equals(((ParamPlaceholder) obj).getName());
      }
   }

   private final Comparable constantValue;

   public ConstantValueExpr(Comparable constantValue) {
      if (constantValue == null) {
         throw new IllegalArgumentException("constantValue cannot be null");
      }
      this.constantValue = constantValue;
   }

   public Comparable getConstantValue() {
      if (isParameter()) {
         throw new IllegalStateException("The value is a parameter " + constantValue);
      }
      return constantValue;
   }

   public boolean isParameter() {
      return constantValue instanceof ParamPlaceholder;
   }

   public Comparable getConstantValueAs(Class<?> targetType, Map<String, Object> namedParameters) {
      Comparable value;
      if (constantValue instanceof ParamPlaceholder) {
         String paramName = ((ParamPlaceholder) constantValue).getName();
         value = (Comparable) namedParameters.get(paramName);
         if (value == null) {
            throw new IllegalStateException("Missing value for parameter " + paramName);
         }
      } else {
         value = constantValue;
      }
      Class<?> type = value.getClass();
      if (type != targetType) {
         if (targetType == String.class) {
            return value.toString();
         } else if (targetType == Boolean.class) {
            if (type == String.class) {
               return Boolean.valueOf((String) value);
            }
         } else if (targetType == Double.class) {
            if (type == String.class) {
               return Double.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).doubleValue();
            }
         } else if (targetType == Float.class) {
            if (type == String.class) {
               return Float.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).floatValue();
            }
         } else if (targetType == Long.class) {
            if (type == String.class) {
               return Long.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).longValue();
            }
         } else if (targetType == Integer.class) {
            if (type == String.class) {
               return Integer.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).intValue();
            }
         } else if (targetType == Short.class) {
            if (type == String.class) {
               return Short.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).shortValue();
            }
         } else if (targetType == Byte.class) {
            if (type == String.class) {
               return Byte.valueOf((String) value);
            } else if (Number.class.isAssignableFrom(type)) {
               return ((Number) value).byteValue();
            }
         }

         //todo [anistor] continue with other commonsense conversions

         //todo [anistor] report illegal conversion
      }
      return value;
   }

   @Override
   public <T> T acceptVisitor(Visitor<?, ?> visitor) {
      return (T) visitor.visit(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ConstantValueExpr other = (ConstantValueExpr) o;
      return constantValue.equals(other.constantValue);
   }

   @Override
   public int hashCode() {
      return constantValue.hashCode();
   }

   @Override
   public String toString() {
      return "CONST(" + constantValue + ')';
   }

   @Override
   public String toJpaString() {
      if (constantValue instanceof ParamPlaceholder) {
         return constantValue.toString();
      }
      if (constantValue instanceof String) {
         return "\"" + constantValue + "\"";
      }
      if (constantValue instanceof Character) {
         return "'" + constantValue + "'";
      }
      if (constantValue instanceof Date) {
         return "'" + DateHelper.getJpaDateFormat().format((Date) constantValue) + "'";
      }
      return "" + constantValue;
   }
}
