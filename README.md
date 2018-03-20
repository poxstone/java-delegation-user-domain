# Simple delegation email for spreadsheet
Query to spreadsheet id with query

## Prepare

1. Create new service account and edit with "Enable G Suite Domain-wide Delegation" permissions
1. Enable Spreadsheet API
1. Download and paste P12 file in src/main/resources/delegate_service_account.p12 
1. add Client Secret ID To G Suite Panel in autorized service accounts and Scopes

## Install

    gradle eclipse
    gredle build
    gradle run
