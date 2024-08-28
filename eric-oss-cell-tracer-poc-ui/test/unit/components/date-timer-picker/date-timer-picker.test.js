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
import { expect, fixture, html } from '@open-wc/testing';
import DateTimerPicker from '../../../../src/components/date-timer-picker/date-timer-picker.js';

describe('DateTimerPicker Component Tests', () => {
  before(() => {
    DateTimerPicker.register();
  });

  describe('Basic component setup', () => {
    it('should render <e-date-timer-picker>', async () => {
      const component = await fixture(
        html`<e-date-timer-picker label="start"></e-date-timer-picker>`,
      );
      const headingTag = component.shadowRoot.querySelector('label');

      expect(
        headingTag.textContent,
        '"Your component markup goes here" was not found',
      ).to.equal('start');
    });
  });
});
