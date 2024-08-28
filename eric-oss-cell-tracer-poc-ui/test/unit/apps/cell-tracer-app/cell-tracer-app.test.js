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
 * Integration tests for <e-cell-tracer-app>
 */
import { expect, fixture } from '@open-wc/testing';
import '../../../../src/apps/cell-tracer-app/cell-tracer-app.js';

describe('Cell Tracer Application Tests', () => {
  let element;
  let multiPanelTile;

  before(async () => {
    element = await fixture('<e-cell-tracer-app></e-cell-tracer-app>');
    multiPanelTile = element.shadowRoot.querySelector('eui-multi-panel-tile');
  });

  describe('Basic application setup', () => {
    it('should create a new <e-cell-tracer-app>', async () => {
      expect(
        multiPanelTile.getAttribute('tile-title'),
        '"Cell Trace" was not found',
      ).to.equal('Table View');
    });

    it('should be able to open the filter tile-panel', async () => {
      const actionIcons = multiPanelTile.querySelectorAll('eui-panel-button');

      actionIcons.forEach(icon => {
        icon.click();
      });

      const traceCalendar =
        multiPanelTile.shadowRoot.querySelector('#traceCalendar');
      const nodeSelect =
        multiPanelTile.shadowRoot.querySelector('e-node-select');
      expect(traceCalendar, "Calendar isn't rendered after clicking Icon").to
        .exist;

      expect(nodeSelect, "Node Select isn't rendered after clicking Icon").to
        .exist;
    });
  });
});
