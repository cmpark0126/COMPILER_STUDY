<script_start>
var passes = 0;
student = 1,
result;
var number, sum = 0;
var input1 = window.prompt("Enter the first number");
var value1 = parseFloat(input);
while(student <= 5) {
    result = window.prompt("Enter result");
    if(result == "1")
        if (result == "2") {passes = passes + 1; square(result);}
        else
        failures++;
        ++student;
}
document.writeln("<h1>Examination Result</h1>");
document.writeln("<h2>Passed and Failed Numbers</h2>");
if(passes > 8)
    document.writeln("<br/>Raise Tuition");

for(number = 2; number <= 100; number++)
    sum += number;
    document.write("The sum of the even integers from 2 to 100 is ");
    document.writeln(sum);


switch(choice) {
    case "1":
        startTag = "<ul>";
        endTag = "</ul>";
        break;
    case "2":
        startTag = "<ol>";
        endTag = "</ol>";
        listType = "<h3>Ordered List: Numbered</h3>";
        break;
    case "3":
        startTag = "<ul>";
        endTag = "</ul>";
        listType = "<h3>Ordered List: Lettered</h3>";
        break;
    default:
        validInput = false;
}

do {
    document.writeln();
    ++counter;
} while(counter <= 6);

function square(y)
{
    return y*y;
}
<script_end>