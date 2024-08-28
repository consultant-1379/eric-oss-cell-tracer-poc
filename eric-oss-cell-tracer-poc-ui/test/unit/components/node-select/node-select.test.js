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
import { expect, fixture } from '@open-wc/testing';
import NodeSelect from '../../../../src/components/node-select/node-select.js';

describe('NodeSelect Component Tests', () => {
  before(() => {
    NodeSelect.register();
  });

  describe('Basic component setup', () => {
    it('should render <e-node-select>', async () => {
      const component = await fixture('<e-node-select></e-node-select>');
      const tree = component.shadowRoot.querySelector('eui-tree');

      expect(
        tree.getAttribute('multi-select'),
        '"Multi-select" tree was not found',
      ).exist;
    });
  });
});
