# Illumio technical assessment

## Task description
Write a program that can parse a file containing flow log data and maps each row to a tag based on a lookup table.  
The lookup table is defined as a csv file, and it has 3 columns, `dstport,protocol,tag`.  
The `dstport` and `protocol` combination decide what tag can be applied.  
Sample flow logs have default format and include version 2 only.

## Assumptions
- only default format of logs is supported
- only version 2 fields are supported
- lookup csv file has described structure and valid
- no need to save tagged rows or any of their fields (just count them)
- if `dstport` and `protocol` doesn't match any tags, incrementing "Untagged" stat
- assuming development speed is the most important
- assuming we need the solution as a simple tool

## Log file format
https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html#flow-logs-fields
"Each record is a string with fields separated by spaces"
"The default format includes all version 2 fields, in the same order that they appear in the table."

## Protocol matching
https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml


