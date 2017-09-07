# **Nadia** Rule/Inference Engine
Much simpler and easy Rules Engine not only to use but also maintain 'Rules/Policy' for businesses

## 1. Introduction
This project is building a Rules(Policies)/Inference Engine with ease of use and maintain rules/policies. It aims to be:

* A rule author is allowed to write rules or policies in a plain text file for the engine rule parser
* A rule author or business persion does NOT need to implement the rules/policies separately like other rules engines
* A user of the engine can carry out Foward-chaining and Backward-chaining with a given rule/policy set

## 2. Roadmap
Add more features as follows;

* GUI for Rule IDE (it is just more than editor. working as an development IDE)
* Retrieving Rule/Policy file from database
* Workflow engine with GUI based diagram editor 
* Machine Learning type inference mechanism


## 3. Make your own Rules/Policies
Please have a look at a file of testing rule in the repository

## 4. How does it work
There are a number of key components as follows;

* Rule reader     : reads a rule/policy file, stream source, string source
* Rule scanner    : scans what 'Rule reader' reads
* Rule parser     : parses what 'Rule scanner' scans into a rule/policy graph
* TopoSort        : sorts the graph parsed by 'Rule parser'
* Inferece Engine : checking all truth or calculated value within forward-chaining, and retrieving next rule/policy within backward-chaining to check in order to complete rule set logic

### How Backward-chaining and Forward-chaining work
Suppose there are following rules:


IF B or C is true THEN A is true.
IF D and E are true THEN C is true.
IF F is true THEN D is false.
IF G is false THEN E is true.

#### Backward-chaining:
An inference engine when using backward chaining searches the inference rules until it finds one which has a consequent (Then clause) that matches a desired goal. For instance, if we want to know whether or not the rule statement of 'A is true' is true, an engine finds out which rule has to be checked to conclude. In this case, the engine needs information about the rule statement of 'B is true', or 'F is true' and 'G is false'

#### Forward-chaining
An inference engine using forward chaining searches the inference rules until it finds one where the antecedent (If clause) is known to be true. For instance, if we do have facts that 'G is false' statement is false and 'F is true'statement is true then the engine concludes as follows;
* 'G is false' statement is false
* 'F is true' statement is true
* 'E is true' statement is false
* 'D is false' statement is true
* 'C is true' statement is false
* 'B is true' statement is unknown
* 'A is true' statement is unknown

## 5. License
Nadia is open source project and released under AGPL 3.0 License.
