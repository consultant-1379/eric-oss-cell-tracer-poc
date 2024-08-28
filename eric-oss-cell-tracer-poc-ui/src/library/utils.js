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
 * Converts a Date object to a string from format by replace.
 * E.g. 'yyyy-MM-dd HH:mm' => '2019-10-01 14:29'
 * y   = year   (yy or yyyy)
 * M   = month (1-12) (MM)
 * d   = day in month (dd)
 * a   = Am/pm marker (AM/PM)
 * h   = hour (0-12)  (hh)
 * H   = hour (0-23)  (HH)
 * m   = minute in hour (mm)
 * s   = seconds (ss)
 * S   = milliseconds (SSS)
 * Z   = time zone, time offset (e.g. -0800)
 *
 * @param {Date} dateObject
 * @param {string} dateFormat
 *
 * @returns {string} The date in the form 2019-10-01 14:29
 */

const pad2 = number => (number < 10 ? `0${number}` : number);

export function simpleDateFormat(dateObject, dateFormat) {
  let text = dateFormat;

  const simpleDateFormats = [
    { pattern: 'yyyy', dateString: `${dateObject.getFullYear()}` },
    {
      pattern: 'yy',
      dateString: `${dateObject.getFullYear()}`.substring(0, 2),
    },
    { pattern: 'MM', dateString: `${pad2(1 + dateObject.getMonth())}` },
    { pattern: 'dd', dateString: `${pad2(dateObject.getDate())}` },
    { pattern: 'a', dateString: `${dateObject.getHours() < 12 ? 'AM' : 'PM'}` },
    { pattern: 'hh', dateString: `${pad2(dateObject.getHours() % 12)}` },
    { pattern: 'HH', dateString: `${pad2(dateObject.getHours())}` },
    { pattern: 'mm', dateString: `${pad2(dateObject.getMinutes())}` },
    { pattern: 'ss', dateString: `${pad2(dateObject.getSeconds())}` },
    { pattern: 'SSS', dateString: `${pad2(dateObject.getMilliseconds())}` },
    { pattern: 'Z', dateString: `${dateObject.getTimezoneOffset()}` },
  ];

  simpleDateFormats.forEach(formatter => {
    text = text.replace(
      new RegExp(formatter.pattern, 'g'),
      formatter.dateString,
    );
  });
  return text;
}
