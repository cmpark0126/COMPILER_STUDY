<script_start>
var temperature = 20;
var limit = 40, fan = 0;
// temperature monitoring
while (temperature <= limit) {
if (temperature == limit) {
document.writeln("temperature limit");
temperature = 20;
fan = 1;
}
else
{
temperature++;
fan = 0;
}
}
<script_end>
