//sample5.jss
<script_start>
var temperature = 20;
var limit = 40, fan = 0;
var choice = 0;
var accumulation;
choice = 1;
switch ( choice ) {
    case "1":
        limit = 10;
        fan = 5;
        break;
    case "2":
        limit = 20;
        fan = 10;
        break;
    case "3":
        limit = 30;
        fan = 20;
        break;
    default:
        limit = 40;
}
<script_end>
