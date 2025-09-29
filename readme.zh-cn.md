# BrainFuckJ
[![brain-fuck](https://img.shields.io/badge/brain-fuck-blue.svg)]()
[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/MegumiKasuga/BrainFuckJ/blob/master/readme.md)
[![zh-cn](https://img.shields.io/badge/lang-汉语-green.svg)](https://github.com/MegumiKasuga/BrainFuckJ/blob/master/readme.zh-cn.md)

## 简介
一个BrainFuck编程语言扩展集。

## 如何使用
首先, 你要有 `java17` 以及一个bfj jar文件. 接着:
### 使用Shell指令参数启动
```shell
java -jar BrainFuckJ-version.jar
```
#### 参数
1. 内存大小
```shell
mem=128  # 从1到256，默认为256
```
2. 最大栈深度
```shell
stack=32  # 从0到65535, 默认为32, 使用负数值来描述不限深度。
```
3. 输入模式
```shell
input_mode=NUMBER  # NUMBER 模式，仅接受数字输入。
input_mode=CHAR    # CHAR 模式，接受字符输入。
```
4. 输出模式
```shell
output_mode=NUMBER  # 直接输出内存块里的数字。
output_mode=CHAR    # 将内存块里的数字转换成char字符再输出
```

5. 脚本路径
```shell
path=你的/代码/的/路径  
# 如果该参数未指定，bfj会以命令行模式启动。
```

#### 范例
```shell
java -jar BrainFuckJ-version.jar path=path/of/your/script input_mode=NUMBER mem=128
```

### 使用 toml 指令参数启动
不带参数首次启动bfj时，会自动生成一个配置文件`config.toml`。
<br> 参数定义和上述一致。
```toml
output_mode = "CHAR"
max_stack_depth = 32
input_mode = "CHAR"
mem_size = 256
```

#### 范例
```shell
java -jar BrainFuckJ-version.jar path=path/of/your/script  
# 如果'path'参数未指定，bfj会以命令行模式启动。
```

### 命令行界面
```shell
Welcome!  # 欢迎消息
BFJ initialized with config:  # 展示当前环境配置
  memory size: 256,
  stack depth: 32,
  input mode: CHAR,
  output mode: CHAR
<BFJ command line started, type 'exit' to exit.>
=>   ,>,[<+>-]<.   # 在这里输入代码
input> 10          # 输入数据
input> 4         
>                  # 输出数据
=>   exit          # 退出
<BFJ terminated.>  # bfj 终止消息
```


## 符号
'pointer' 用以指代内存指针。
<br>'cell\[pointer\]' 指代当前内存里的数据。
<br>'cp' 指代程序指针。

| 符号    | 意涵                                                                                                                            | 集合 |
|-------|-------------------------------------------------------------------------------------------------------------------------------|----|
| +     | cell\[pointer\] += 1                                                                                                          | 原生 |
| -     | cell\[pointer\] -= 1                                                                                                          | 原生 |
| (     | cell\[pointer\] <<= 1                                                                                                         | 扩展 |
| )     | cell\[pointer\] >>= 1                                                                                                         | 扩展 |
| @     | cell\[pointer\] = 0                                                                                                           | 扩展 |
| \<    | pointer -= 1                                                                                                                  | 原生 |
| \>    | pointer += 1                                                                                                                  | 原生 |
| ^     | pointer = cell\[pointer\]                                                                                                     | 扩展 |
| \|    | pointer = 0                                                                                                                   | 扩展 |
| !     | 清空内存                                                                                                                          | 扩展 |
| [     | 若 cell\[pointer\] > 0, 运行循环; 否则, 跳到其所对应的 '\]' 后面一个符号处。                                                                        | 原生 |
| ]     | 循环末尾符, 跳到其所对应的循环头符号 '\['                                                                                                      | 原生 |
| $     | 代码标记, 一个脚本中最多有256个。                                                                                                           | 扩展 |
| *     | cp = mark\[cell\[pointer\]\]                                                                                                  | 扩展 |
| {     | 压栈跳转, cp = mark\[cell\[pointer\]\], 然后将当前符号入栈                                                                                 | 扩展 |
| }     | 弹栈跳转, 将栈顶'{'取出, 跳转到它的后一个符号处                                                                                                   | 扩展 |
| ,     | cell\[pointer\] = input()<br>1. 回车键 = 10<br>2. 若输入值长度为1, 输入会被处理成char.<br>3. 输入长度 > 1，处理成0~255的数字, 若为NaN, 报错                   | 原生 |
| .     | 输出 cell\[pointer\]                                                                                                            | 原生 |
| %     | 复制, cell\[pointer\] = cell\[cell\[pointer\]\]                                                                                 | 扩展 |


## 数据
bfj中的数据是0~255的无符号整数。输入/输出时自动转换为对应的char字符(使用ascii码表)。
<br> 溢出处理规则:
<br> ~> 255 + 1 = 0
<br> ~> 0 - 1 = 255

## 内存
内存是一块容量最小为1，最大为256的ubyte数组(使用short来实现ubyte)。
它有一个内存指针(从0到mem.length - 1)。
<br> 溢出处理规则:
<br> ~> 0 - 1 = mem.length - 1
<br> ~> (mem.length - 1) + 1 = 0

## 范例
这是一个使用bfj实现函数调用，将两个值加起来并输出的程序：
```BrainFuck
+*  # 跳转到代码标记 1
$  # 代码标记 0  
|> [<+>-]  # pointer = 1, 循环进行 {mem[0] + 1, mem[1] - 1} 直至 mem[1] == 0  
|  # pointer = 0  
}  # 弹栈跳转  
  
$  # 代码标记 1  
@ ++ > +(( >  # mem[0] = 2, mem[1] = 4  
{  # 跳转到代码标记 0, (mem[2] = 0) 
.  # 弹栈跳转后，代码应该来到这里并输出结果。
```