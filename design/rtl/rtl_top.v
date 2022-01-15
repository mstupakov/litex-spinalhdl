module SuperRtlTop(
  input clk_25mhz,
  input rst,

  input clk_5MHz,
  input clk_10MHz,
  input clk_20MHz,

  input  [4:0] btns,
  output [7:0] leds,

  input  uart_rx,
  output uart_tx
);

  reg [20:0] counter;
  always @(posedge clk_5MHz)
    counter <= counter + 1;

  assign leds[4:0] = btns;
  assign leds[7:5] = counter[20:18];

endmodule
