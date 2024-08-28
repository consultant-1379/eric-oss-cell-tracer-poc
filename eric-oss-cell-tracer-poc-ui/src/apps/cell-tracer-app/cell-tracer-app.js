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
 * CellTracerApp is defined as
 * `<e-cell-tracer-app>`
 *
 * @extends {App}
 */
import { App, html, definition } from '@eui/app';
import { ActionableIcon, Button, Datepicker, TextField } from '@eui/base';
import { Icon } from '@eui/theme/icon';
import { Tile, MultiPanelTile, TilePanel } from '@eui/layout';
import style from './cell-tracer-app.css';
import DateTimerPicker from '../../components/date-timer-picker/date-timer-picker.js';
import NodeSelect from '../../components/node-select/node-select.js';
import TraceTable from '../../components/trace-table/trace-table.js';

export default class CellTracerApp extends App {
  static get components() {
    return {
      'eui-datepicker': Datepicker,
      'eui-button': Button,
      'eui-icon': Icon,
      'eui-actionable-icon': ActionableIcon,
      'eui-tile': Tile,
      'eui-multi-panel-tile': MultiPanelTile,
      'eui-tile-panel': TilePanel,
      'eui-text-field': TextField,
      'e-node-select': NodeSelect,
      'e-trace-table': TraceTable,
      'e-date-timer-picker': DateTimerPicker,
    };
  }

  didUpgrade() {
    const now = new Date();
    const start = this.shadowRoot.querySelector(
      'e-date-timer-picker[date-type="start"]',
    );
    const end = this.shadowRoot.querySelector(
      'e-date-timer-picker[date-type="end"]',
    );
    end.setValue(now);
    start.setValue(this.adjustTimeForStart(now));
  }

  didConnect() {
    this.bubble('app:lineage', { metaData: this.metaData });
    this.bubble('app:subtitle', { subtitle: '' });
    this.fetchData('trace_sample.json');
  }

  adjustTimeForStart(date) {
    date.setMinutes(date.getMinutes() - 10);
    return date;
  }

  fetchData(dataFile) {
    fetch(dataFile)
      .then(response => response.json())
      .then(data => {
        this.traceSampleJSON = data;
        this.data = TraceTable.createArray(this.traceSampleJSON);
      })
      .catch(error => {
        console.error(error);
      });
  }

  fetchValues() {
    this.endDate = this.shadowRoot
      .querySelector('e-date-timer-picker[date-type="end"]')
      .getValue();
    this.startDate = this.shadowRoot
      .querySelector('e-date-timer-picker[date-type="start"]')
      .getValue();

    this.ueTrace = this.shadowRoot.querySelector('#ueTraceText').value;
    this.traceRecordText =
      this.shadowRoot.querySelector('#traceTextField').value;
    const nodeSelect = this.shadowRoot.querySelector('e-node-select');
    this.nodeValues = nodeSelect.getNodeValues();
  }

  clearInput() {
    const textFields = this.shadowRoot.querySelectorAll('eui-text-field');

    for (const field of textFields) {
      field.value = '';
    }
  }

  render() {
    return html`
     <eui-multi-panel-tile tile-title="Table View">
        <eui-tile-panel
          tile-title="Filter"
          slot="left"
          icon-name="filter"
          width="525"
          show
        >
          <div slot="content">
            <eui-tile>
              <div slot="content">
                <div>
                <div id="timePickerContainer">
        <div id="traceCalendar">
          <e-date-timer-picker
            date-type="start"
            label="Start"
          ></e-date-timer-picker>
          <e-date-timer-picker
            date-type="end"
            label="End"
          ></e-date-timer-picker>
        </div>
      </div>
      <div class="inputContainer">
        <div class="traceInput">
          <eui-text-field
            id="ueTraceText"
            name="item"
            type="text"
            prefix="ueTraceId"
            size="14"
            placeholder="base64"
          >
          </eui-text-field>
        </div>
        <div class="traceInput">
          <eui-text-field
            name="item"
            type="text"
            prefix="traceRecordingSessionReference"
            id="traceTextField"
            size="15"
            placeholder="base64"
          ></eui-text-field>
        </div>
        <eui-button id="clearButton" @click="${() => this.clearInput()}"
        >Clear</eui-button
      >
      </div>
      
                </div>
                <e-node-select id="nodeSelect"></e-node-select>
              </div>
          </div>
        <eui-button 
        primary 
        id="fetchButton"
        @click="${() => this.fetchValues()}"
        slot="footer"
        >Fetch Traces
        </eui-button>
        </eui-tile-panel>
        <div slot="content">
          <e-trace-table .data="${this.data}"></e-trace-table>
        </div>
      </eui-multi-panel-tile>
      
    `;
  }
}

definition('e-cell-tracer-app', {
  style,
  props: {
    data: { type: Array },
    columns: { type: Array },
    nodeValues: { type: Array },
    endDate: { type: Number, default: null }, // epoch time in ms
    startDate: { type: Number, default: null }, // epoch as well
    ueTrace: { type: String, default: 'ueTraceText' },
    traceRecordText: { type: String, default: 'traceRecordText' },
  },
})(CellTracerApp);

CellTracerApp.register();
