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
import { html, fixture, expect } from '@open-wc/testing';
import TraceTable from '../../../../src/components/trace-table/trace-table.js';

describe('TraceTable Component Tests', () => {
  let template;
  let data;
  let row;
  before(() => {
    TraceTable.register();
    row = {
      col1: 12345678,
      col2: 'gnodebName',
      col3: 'eventTypeName',
      col4: 'nwFunction',
      col5: 'XXXXX',
      col6: 'Jake',
      col7: 'Flavio',
      col8: JSON.stringify('{key : "value" }', null, 2),
    };
    data = [row, row];
    template = html`<e-trace-table .data=${data}></e-trace-table>`;
  });

  describe('Basic component setup', () => {
    it('should render <e-trace-table>', async () => {
      const component = await fixture('<e-trace-table></e-trace-table>');
      const tile = component.shadowRoot.querySelector('eui-tile');

      expect(
        tile.getAttribute('tile-title'),
        '"Cell Trace" was not found',
      ).to.equal('Cell Trace');
    });

    it('should place content into all columns correctly', async () => {
      const component = await fixture(template);
      const euiTable = component.shadowRoot.querySelector('eui-table');
      const table = euiTable.shadowRoot.querySelector('tbody');

      const tableRows = table.querySelectorAll('tr');
      expect(
        tableRows.length,
        'Not all rows have been rendered in',
      ).to.be.equal(2);

      const firstRow = tableRows[0];
      const tableCells = firstRow.querySelectorAll(
        'td:not(.expandable-control)',
      );

      const rowValues = Object.values(row);
      const stringValues = rowValues.map(element => element.toString());

      const colsMatch = Array.prototype.every.call(tableCells, td => {
        const tdCol = td.querySelector('span').textContent;
        return stringValues.includes(tdCol);
      });

      expect(colsMatch).to.be.true;
    });

    it('should allow row to be expanded and values of CodeSnippet to be read', async () => {
      const component = await fixture(template);
      const euiTable = component.shadowRoot.querySelector('eui-table');
      const table = euiTable.shadowRoot.querySelector('tbody');

      const expandableControl = table.querySelector('td');
      expandableControl.click();
      await new Promise(resolve => {
        setTimeout(resolve, 0);
      });
      // needed to let click action render the expanded rows and <e-dsg-code-snippet>

      const codeSnip = table.querySelector('e-dsg-code-snippet');
      expect(codeSnip).to.exist;
    });
  });
});
