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
 * Component NodeSelect is defined as
 * `<e-node-select>`
 *
 * @extends {LitComponent}
 */
import { LitComponent, html, definition } from '@eui/lit-component';
import { Button, TextField, Tree, TreeItem } from '@eui/base';
import style from './node-select.css';

export default class NodeSelect extends LitComponent {
  static get components() {
    return {
      ...super.components, // if extending
      'eui-tree': Tree,
      'eui-tree-item': TreeItem,
      'eui-text-field': TextField,
      'eui-button': Button,
    };
  }

  /**
   * Render the <e-node-select> component. This function is called each time a
   * prop changes.
   */
  searchFilter() {
    const tree = this.shadowRoot.querySelectorAll('eui-tree-item');
    const searchValue = this.shadowRoot.querySelector('.searchFilter').value;
    for (const item of tree) {
      const itemContent = item.textContent;
      if (!itemContent.includes(searchValue)) {
        item.setAttribute('hidden', '');
      } else {
        item.removeAttribute('hidden');
      }
    }
  }

  getNodeValues() {
    const tree = this.shadowRoot.querySelectorAll(
      'eui-tree-item:not(.non-select)',
    );

    const selectedNodes = Array.from(tree)
      .filter(
        node => node.hasAttribute('checked') && !node.hasAttribute('hidden'),
      )
      .map(node => node.textContent.trim());

    return selectedNodes;
  }

  render() {
    return html`
      <div class="container">
        <eui-text-field
          placeholder="Search"
          class="searchFilter"
        ></eui-text-field>
        <eui-button
          primary
          icon="search"
          @click="${() => this.searchFilter()}"
          class="searchButton"
        ></eui-button>
        <eui-button primary>Refresh</eui-button>
      </div>
      <eui-tree multi-select id="nodeTree">
        <eui-tree-item>Node</eui-tree-item>
        <eui-tree-item class="non-select"
          >Trace Level
          <eui-tree-item>CELL</eui-tree-item>
          <eui-tree-item>GNODEB</eui-tree-item>
          <eui-tree-item>UE</eui-tree-item>
        </eui-tree-item>
        <eui-tree-item class="non-select"
          >Node Function Type
          <eui-tree-item>CUCP</eui-tree-item>
          <eui-tree-item>CUUP</eui-tree-item>
          <eui-tree-item>DU</eui-tree-item>
          <eui-tree-item>NO_VALUE</eui-tree-item>
          <eui-tree-item>UNRECOGNIZED</eui-tree-item>
        </eui-tree-item>
        <eui-tree-item class="non-select"
          >PM Event
          <eui-tree-item>CuCPAnrEval</eui-tree-item>
          <eui-tree-item>CuCpDrbSuspend</eui-tree-item>
          <eui-tree-item>CuCpEcgiMeasurement</eui-tree-item>
          <eui-tree-item>CuCpDrbMeasurementInitiationRequest</eui-tree-item>
        </eui-tree-item>
      </eui-tree>
    `;
  }
}

definition('e-node-select', {
  style,
})(NodeSelect);
