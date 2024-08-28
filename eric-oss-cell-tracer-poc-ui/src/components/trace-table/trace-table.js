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
 * Component TraceTable is defined as
 * `<e-trace-table>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Table } from '@eui/table';
import { Button, ActionableIcon } from '@eui/base';
import { Tile } from '@eui/layout';
import { CodeSnippet } from '@docsitegen/components';
import style from './trace-table.css';

export default class TraceTable extends LitComponent {
  constructor() {
    super();
    this.columns = [
      { title: 'Timestamp', attribute: 'col1', sortable: true, width: '200px' },
      { title: 'Node', attribute: 'col2', sortable: true, width: '800px' },
      { title: 'Type', attribute: 'col3', sortable: true, width: '260px' },
      {
        title: 'Network nFunction',
        attribute: 'col4',
        sortable: true,
        width: '60px',
      },
      { title: 'Level', attribute: 'col5', sortable: true, width: '85px' },
      { title: 'uetraceId', attribute: 'col6', sortable: true },
      {
        title: 'traceRecordingSessionReference',
        attribute: 'col7',
        sortable: true,
      },
    ];
  }

  static get components() {
    return {
      ...super.components,
      'eui-table': Table,
      'eui-button': Button,
      'eui-actionable-icon': ActionableIcon,
      'eui-tile': Tile,
      'e-dsg-code-snippet': CodeSnippet,
    };
  }

  static createArray(arrayData) {
    const data = arrayData.map(value => ({
      col1: value.timestamp,
      col2: value.gnodebName,
      col3: value.eventTypeName,
      col4: value.nwFunction,
      col5: value.trcLevel,
      col6: '',
      col7: '',
      col8: JSON.stringify(value.body, null, 2),
    }));
    return data;
  }

  onclickFilter() {
    this.filterValue = !this.filterValue;
  }

  /**
   * Render the <e-trace-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-tile tile-title="Cell Trace" subtitle="0 of 13 selected">
        <eui-actionable-icon
          name=${this.filterValue ? 'filter-off' : 'filter'}
          @click=${event => this.onclickFilter(event.currentTarget.value)}
          slot="action"
        ></eui-actionable-icon>
        <div slot="content">
          <eui-table
            .columns=${this.columns}
            .data=${this.data}
            slot="content"
            ?filterable="${this.filterValue}"
            expandable
            sortable
            resizable
            custom-row-height="32"
            .components=${{
              'e-dsg-code-snippet': CodeSnippet,
            }}
            .custom=${{
              onCreatedDetailsRow: row => html`
                <e-dsg-code-snippet language="json" code="${row.col8}" copy>
                </e-dsg-code-snippet>
              `,
            }}
          >
          </eui-table>
        </div>
        <eui-button primary slot="footer">Refresh</eui-button>
      </eui-tile>
    `;
  }
}

definition('e-trace-table', {
  style,
  props: {
    data: { type: Array },
    columns: { type: Array },
    filterValue: { type: Boolean, default: false },
    dataFile: { type: String, default: null },
  },
})(TraceTable);
