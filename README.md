# What is Gality?

GaLity is an open-source implementation to compute metrics on sets of gadgets which has been developed by Andreas Follner.
If you use Gality in your research, please cite the following paper:

**Andreas Follner, Alexandre Bartel, Eric Bodden: Analyzing the Gadgets - Towards a Metric to Measure Gadget Quality, in Proceedings of the International Symposium on Engineering Secure Software and Systems (ESSoS), London, UK, 2016** [\[bib\]](https://www.abartel.net/static/p/essos2016-analyzingGadgets.bib.txt) [\[pdf\]](https://www.abartel.net/static/p/essos2016-analyzingGadgets.pdf)

# How to compile Gality?

## With Eclipse

The easiest way is to import the git project into Eclipse and to let it compile it automatically. 

## Command Line

You can also compile the program on the command line:
```bash
$ cd gality
$ javac -d ./bin/ src/gality/Program.java
```

# How to run Gality?

The first argument is the file containing the set of gadgets generated by [ROPgadget](https://github.com/JonathanSalwan/ROPgadget).
The second argument is Gality's output file.

# Eclipse

You can use a "run configuration" to give parameters to gality and then run gality.

# Command Line

```bash
$ ROPgadget --binary /usr/bin/whereis > /tmp/whereis.gadgets
$ java -cp ./bin/ gality.Program /tmp/whereis.gadgets /tmp/whereis.gadgets.metrics
```
