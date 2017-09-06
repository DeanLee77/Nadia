package factValuePackage;

import java.time.LocalDate;


public class FactDateValue extends FactValue {
		
		private LocalDate value;
		private LocalDate defaultValue;
		
		public FactDateValue(LocalDate date) {
			setValue(date);
		}

		public void setValue(LocalDate cal) {
			this.value = cal;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public LocalDate getValue() {
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public LocalDate getDefaultValue()
		{
			return this.defaultValue;
		}
		

		@Override
		public FactValueType getType() {
			return FactValueType.DATE;
		}

		@Override
		public <T> void setDefaultValue(T defaultValue) {
			this.defaultValue = (LocalDate)defaultValue;		
		}
}
