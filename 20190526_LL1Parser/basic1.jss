<script_start>
var temperature = 20 , change = 0;
var limit = 40, fan = 0;
limit++;
temperature = temperature + change;
limit = limit + fan;
if(temperature > limit )
{
    temperature = 20;
    limit = limit – 5;
}
else
{
    temperature = temperature + 10;
    limit = limit + 5;
}
<script_end>
