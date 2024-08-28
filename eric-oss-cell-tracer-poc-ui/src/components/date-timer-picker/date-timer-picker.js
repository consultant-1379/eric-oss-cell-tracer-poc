/*
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
/**
 * Component DateTimerPicker is defined as
 * `<e-date-timer-picker>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Datepicker, TextField } from '@eui/base';
import { simpleDateFormat } from '../../library/utils.js';
import style from './date-timer-picker.css';

export default class DateTimerPicker extends LitComponent {
  static get components() {
    return {
      ...super.components, // if extending
      'eui-datepicker': Datepicker,
      'eui-text-field': TextField,
    };
  }

  setValue(date) {
    if (!(date instanceof Date) || date === undefined) {
      date = new Date();
    }
    if (date && this.dateString === 'currentDate') {
      this.dateString = simpleDateFormat(date, 'yyyy-MM-dd');
    }

    if (date && this.time === 'currentTime') {
      this.time = simpleDateFormat(date, 'HH:mm:ss');
    }
  }

  getValue() {
    let dateTimeInput;
    if (this.shadowRoot) {
      dateTimeInput = `${this.shadowRoot.getElementById('date').date}T${
        this.shadowRoot.getElementById('time').value
      }`;

      switch (this.format) {
        case 'epoch':
          return new Date(dateTimeInput).getTime();
        case 'text':
          return dateTimeInput;
        case 'iso':
          return new Date(dateTimeInput).toISOString();
        default:
          console.error(`Unknown format: ${this.format}`);
          return dateTimeInput;
      }
    }
    return this.dateTimeInput;
  }

  render() {
    return html`
      <label>${this.label}</label>
      <div class="datePickerContainer">
        <eui-datepicker .date=${this.dateString} id="date"></eui-datepicker>
        <eui-text-field
          size="6"
          .value="${this.time}"
          id="time"
        ></eui-text-field>
      </div>
    `;
  }
}

definition('e-date-timer-picker', {
  style,
  props: {
    dateString: { attribute: false, type: String, default: 'currentDate' },
    dateType: { attribute: true, type: String, default: 'dateType' },
    format: { attribute: false, type: String, default: 'epoch' }, // One of ['epoch', 'text]'
    label: { attribute: true, type: String, default: 'labelName' },
    time: { attribute: false, type: String, default: 'currentTime' },
  },
})(DateTimerPicker);
