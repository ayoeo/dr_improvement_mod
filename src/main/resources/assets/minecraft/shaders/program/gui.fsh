#version 120

uniform sampler2D sampler;

uniform vec2 resolution;
uniform vec4 colourBase;
uniform vec4 colourEnergy;
uniform vec2 circleCenter;
uniform float circleRadius;
uniform float energyPercent;

#define PI 3.14159265

float sdArc(in vec2 p, in vec2 sca, in vec2 scb, in float ra, in float rb) {
  p *= mat2(sca.x, sca.y, -sca.y, sca.x);
  p.x = abs(p.x);
  float k = (scb.y * p.x > scb.x * p.y) ? dot(p.xy, scb) : length(p);
  return sqrt(dot(p, p) + ra * ra - 2.0 * ra * k) - rb;
}

float drawEnergyBar(vec2 uv, float percent, bool flip) {
  float rotation = 90.;
  float arc = 30.;// TODO CHANGE FOR THICKEST HAPPY
  float purr = (1. - percent) * arc;
  float ta = flip ? radians(-90. + purr) : radians(90. - purr);
  float tb = radians(arc - purr);
  float rb = 0.009;// TODO - change for THICKNESS aka also make thick happy ? maybe

  vec2 offset = vec2(0., .125);// TODO - change to OFFSET IT LOL
  return sdArc(uv + (flip ? -offset : offset), vec2(sin(ta), cos(ta)), vec2(sin(tb), cos(tb)), .2, rb);// TODO change .12 to make TINY or BIG??
}

vec4 drawArc(float len, bool filled) {
  vec4 col = vec4(0.);// outline
  vec4 outline = vec4(vec3(0.), colourBase.a);
  vec4 inner = filled ? colourEnergy : colourBase;
  float outlineSize = .003;
  col = mix(col, inner, 1. - smoothstep(0., outlineSize, len + .0025));
  if (!filled) {
    col = mix(col, outline, 1. - smoothstep(0., outlineSize, abs(len)));
  }
  return col;
}

void main() {
  vec2 uv = (gl_FragCoord.xy * 2.0 - resolution) / resolution.y;

  vec4 top = drawArc(drawEnergyBar(uv, 1., false), false);
  float topPercent = min(1., energyPercent * 2.);
  vec4 topEnergy = drawArc(drawEnergyBar(uv, topPercent, false), true);

  vec4 btm = drawArc(drawEnergyBar(uv, 1., true), false);
  float btmPercent = min(1., max(0., (energyPercent - .5) * 2.));
  vec4 btmEnergy = drawArc(drawEnergyBar(uv, btmPercent, true), true);

  vec4 colT = mix(top, topEnergy, topPercent > 0. ? topEnergy.a : 0.);
  vec4 colB = mix(btm, btmEnergy, btmPercent > 0. ? btmEnergy.a : 0.);

  if (colT.a > 0) {
    gl_FragColor = colT;
    gl_FragColor.a *= .75;
  } else if (colB.a > 0) {
    gl_FragColor = colB;
    gl_FragColor.a *= .75;
  }
}

