# BrainFuckJ
[![brain-fuck](https://img.shields.io/badge/brain-fuck-blue.svg)]()
[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/MegumiKasuga/BrainFuckJ/blob/master/readme.md)
[![zh-cn](https://img.shields.io/badge/lang-汉语-green.svg)](https://github.com/MegumiKasuga/BrainFuckJ/blob/master/readme.zh-cn.md)

## Intro
An extended set of the BrainFuck programming language.

## Implementation
#### Maven
```xml
<dependency>
    <groupId>edu.carole</groupId>
    <artifactId>bfj</artifactId>
    <version>${version}</version>
</dependency>

<repository>
<id>bfj</id>
<url>https://raw.github.com/MegumiKasuga/kasuga-maven/bfj-${version}/</url>
</repository>
```
#### Gradle
``` groovy
repositories {
    maven {
        url = "https://raw.github.com/MegumiKasuga/kasuga-maven/bfj/"
    }
}

dependencies {
  implementation group: 'edu.carole', name: 'bfj', version: '${version}'
}
```
#### Version
```
- 1.01
```
#### Details
[- click here to visit repo kasuga-maven.](https://github.com/MegumiKasuga/kasuga-maven)

## How to use
First, you'll need `java17` and the bfj jar. Then:
### Boot with shell arguments
```shell
java -jar BrainFuckJ-version.jar
```
#### Arguments
1. Memory Size
```shell
mem=128  # range from 1 to 256, default 256
```
2. Max Stack Depth
```shell
stack=32  # range from 0 to 65535, default 32, use negative value for inf.
```
3. Input Mode
```shell
input_mode=NUMBER  # NUMBER mode for numeric input only.
input_mode=CHAR    # CHAR mode for char input.
```
4. Output Mode
```shell
output_mode=NUMBER  # directly output the number of the memory cell.
output_mode=CHAR    # output (char) cell[pointer]
```

5. Path
```shell
path=path/of/your/script  
# bfj would go into command mode if there's no 'path' argument
```

#### Example
```shell
java -jar BrainFuckJ-version.jar path=path/of/your/script input_mode=NUMBER mem=128
```

### Boot with toml configs
on the very first time you run the bfj jar(without any argument), 
a toml cfg file `config.toml` would be generated.
<br> The definition of arguments here are just the same as mentioned before.
```toml
output_mode = "CHAR"
max_stack_depth = 32
input_mode = "CHAR"
mem_size = 256
```

#### Example
```shell
java -jar BrainFuckJ-version.jar path=path/of/your/script  
# bfj would go into command mode if there's no 'path' argument
```

### CommandLine View
```shell
Welcome!  # welcome message
BFJ initialized with config:  # show all environment config here.
  memory size: 256,
  stack depth: 32,
  input mode: CHAR,
  output mode: CHAR
<BFJ command line started, type 'exit' to exit.>
=>   ,>,[<+>-]<.   # type your code here
input> 10          # input data
input> 4         
>                  # output data
=>   exit          # exit
<BFJ terminated.>  # terminate message.
```


## Symbols
'pointer' refers to memory pointer.
<br>'cell\[pointer\]' refers to the data in the current cell. 
<br>'cp' refers to program pointer.

| Symbol | Meaning                                                                                                                                                                       | state     |
|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| +      | cell\[pointer\] += 1                                                                                                                                                          | original  |
| -      | cell\[pointer\] -= 1                                                                                                                                                          | original  |
| (      | cell\[pointer\] <<= 1                                                                                                                                                         | extension |
| )      | cell\[pointer\] >>= 1                                                                                                                                                         | extension |
| @      | cell\[pointer\] = 0                                                                                                                                                           | extension |
| \<     | pointer -= 1                                                                                                                                                                  | original  |
| \>     | pointer += 1                                                                                                                                                                  | original  |
| ^      | pointer = cell\[pointer\]                                                                                                                                                     | extension |
| \|     | pointer = 0                                                                                                                                                                   | extension |
| !      | memory clear                                                                                                                                                                  | extension |
| [      | loop on cell\[pointer\] > 0, else, jump to the symbol after its paired '\]'                                                                                                   | original  |
| ]      | loop end, jump to its paired loop head '\['                                                                                                                                   | original  |
| $      | script mark, with a maximum of 256 marks                                                                                                                                      | extension |
| *      | cp = mark\[cell\[pointer\]\]                                                                                                                                                  | extension |
| {      | stack jump, cp = mark\[cell\[pointer\]\], then stack.push(\<this symbol\>)                                                                                                    | extension |
| }      | cp = stack.pop().index + 1                                                                                                                                                    | extension |
| ,      | cell\[pointer\] = input()<br>1. \<enter\> = 10<br>2. if (input.length == 1), input is converted to a char.<br>3. input.length > 1，a number between 0~255, error raised if NaN | original  |
| .      | output(cell\[pointer\])                                                                                                                                                       | original  |
| %      | copy, cell\[pointer\] = cell\[cell\[pointer\]\]                                                                                                                               | extension |


## Data
Data in bfj are all ubytes range from 0 to 255. 
<br> while overflow:
<br> ~> 255 + 1 = 0
<br> ~> 0 - 1 = 255

## Memory
Memory is an array of ubyte values with a minimum capacity of 1 and a maximum capacity of 256 (using short to represent ubyte).
<br>Memory contains a memory pointer (from 0 to mem.length - 1).
<br> while overflow:
<br> ~> 0 - 1 = mem.length - 1
<br> ~> (mem.length - 1) + 1 = 0

## Example
This is an example program that uses BFJ to implement function calls, adding two values together and outputting the result:
```BrainFuck
+*  # jump to script mark 1
$  # script mark 0  
|> [<+>-]  # pointer = 1, loop {mem[0] + 1, mem[1] - 1} until mem[1] == 0  
|  # pointer = 0  
}  # stack.pop() jump  
  
$  # script mark 1  
@ ++ > +(( >  # mem[0] = 2, mem[1] = 4  
{  # jump to script mark 0, (mem[2] = 0) 
.  # after stack.pop() jump, the program approached here, then output the result.
```


