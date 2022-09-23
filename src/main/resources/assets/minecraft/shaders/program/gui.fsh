#version 120

uniform sampler2D sampler;

uniform vec2 resolution;

uniform vec4 colourBase;
uniform vec4 colourEnergy;

uniform vec4 colourHpBase;
uniform vec4 colourHp;

uniform vec4 colourManaBase;
uniform vec4 colourMana;

uniform vec2 circleCenter;

uniform float energyPercent;
uniform float manaPercent;
uniform float healthPercent;

uniform float showHealth;
uniform float showEnergy;
uniform float barWidth;
uniform float yOffset;

#define PI 3.14159265

float udSegment(in vec2 p, in vec2 a, in vec2 b) {
  vec2 ba = b-a;
  vec2 pa = p-a;
  float h = clamp(dot(pa, ba)/dot(ba, ba), 0.0, 1.0);
  return length(pa-h*ba);
}

float sdArc(in vec2 p, in vec2 sca, in vec2 scb, in float ra, in float rb) {
  p *= mat2(sca.x, sca.y, -sca.y, sca.x);
  p.x = abs(p.x);
  float k = (scb.y * p.x > scb.x * p.y) ? dot(p.xy, scb) : length(p);
  return sqrt(dot(p, p) + ra * ra - 2.0 * ra * k) - rb;
}

float drawEnergyBar(vec2 uv, float percent, bool flip) {
  float arc = barWidth;// TODO CHANGE FOR THICKEST HAPPY
  float purr = (1.0 - percent) * arc;
  float ta = flip ? radians(-90.0 + purr) : radians(90.0 - purr);
  float tb = radians(arc - purr);
  float rb = 0.01;// TODO - change for THICKNESS aka also make thick happy ? maybe

  vec2 offset = vec2(0.0, yOffset);// TODO - change to OFFSET IT LOL
  if (showEnergy == 0.0) {
    return 999999.0;
  } else {
    return sdArc(uv + (flip ? -offset : offset), vec2(sin(ta), cos(ta)), vec2(sin(tb), cos(tb)), 0.2, rb);// TODO change .12 to make TINY or BIG??
  }
}

float lenny = 1.3;
float sizez = 0.025;

float benny = 0.2;
float zizez = 0.01;

float drawManaBar(vec2 uv) {
  float startWHY = -150.0;
  float WHY = startWHY / resolution.y;
  vec2 start = vec2(-benny / 2.0, WHY);
  if (showHealth == 0.0) {
    return 999999.0;
  } else {
    return udSegment(uv, start, start + vec2(benny, 0.0)) - zizez; }
}

float drawHealthBar(vec2 uv) {
  float startWHY = resolution.y - 100.0;
  float WHY = startWHY / resolution.y;
  vec2 start = vec2(-lenny / 2.0, WHY);
  if (showHealth == 0.0) {
    return 999999.0;
  } else {
    return udSegment(uv, start, start + vec2(lenny, 0.0)) - sizez; }
}

vec4 drawArc(float len, bool filled) {
  vec4 col = vec4(0.0);
  vec4 outline = vec4(vec3(0.0), colourBase.a);
  vec4 inner = filled ? colourEnergy : colourBase;
  float outlineSize = 0.003;
  col = mix(col, inner, 1.0 - smoothstep(0.0, outlineSize, len + 0.0025));
  if (!filled) {
    col = mix(col, outline, 1.0 - smoothstep(0.0, outlineSize, abs(len)));
  }
  return col;
}

vec4 drawManaArc(float len, bool filled, vec2 uv) {
  vec4 col = vec4(0.0);
  vec4 outline = vec4(vec3(0.0), colourManaBase.a);
  float lenn = (benny + sizez * 2.0);
  float lennW = (benny + sizez * 2.0) * manaPercent;
  float start = -lenn / 2.0;

  vec4 inner;
  if (filled && (uv.x <= start + lennW)) {
    inner = colourMana;
  } else if (filled && (uv.x >= start + lennW)) {
    float mixxy =  uv.x - (start + lennW);
    if (mixxy < 0.005) {
      inner = mix(colourMana, colourManaBase, 200.0 * mixxy);
    } else {
      inner = colourManaBase;
    }
  } else {
    inner = colourManaBase;
  }

  float outlineSize = 0.004;
  col = mix(col, inner, 1.0 - smoothstep(0.0, outlineSize, len + .002));
  if (!filled) {
    col = mix(col, outline, 1.0 - smoothstep(0.0, outlineSize, abs(len)));
  }
  return col;
}

vec4 drawHealthArc(float len, bool filled, vec2 uv) {
  vec4 col = vec4(0.0);
  vec4 outline = vec4(vec3(0.0), colourHpBase.a);
  float lenn = (lenny + sizez * 2.0);
  float lennW = (lenny + sizez * 2.0) * healthPercent;
  float start = -lenn / 2.0;

  vec4 inner;
  if (filled && (uv.x <= start + lennW)) {
    inner = colourHp;
  } else if (filled && (uv.x >= start + lennW)) {
    float mixxy =  uv.x - (start + lennW);
    if (mixxy < 0.005) {
      inner = mix(colourHp, colourHpBase, 200.0 * mixxy);
    } else {
      inner = colourHpBase;
    }
  } else {
    inner = colourHpBase;
  }

  float outlineSize = 0.006;
  col = mix(col, inner, 1.0 - smoothstep(0.0, outlineSize, len + .0025));
  if (!filled) {
    col = mix(col, outline, 1.0 - smoothstep(0.0, outlineSize, abs(len)));
  }
  return col;
}

vec4 drawManaBar(float len, bool filled, vec2 uv) {
  vec4 col = vec4(0.0);
  vec4 outline = vec4(vec3(0.0), colourHpBase.a);
  float lenn = (benny + sizez * 2.0);
  float lennW = (benny + sizez * 2.0) * healthPercent;
  float start = -lenn / 2.0;

  vec4 inner;
  if (filled && (uv.x <= start + lennW)) {
    inner = colourHp;
  } else if (filled && (uv.x >= start + lennW)) {
    float mixxy =  uv.x - (start + lennW);
    if (mixxy < 0.005) {
      inner = mix(colourHp, colourHpBase, 200.0 * mixxy);
    } else {
      inner = colourHpBase;
    }
  } else {
    inner = colourHpBase;
  }

  float outlineSize = 0.006;
  col = mix(col, inner, 1.0 - smoothstep(0.0, outlineSize, len + .0025));
  if (!filled) {
    col = mix(col, outline, 1.0 - smoothstep(0.0, outlineSize, abs(len)));
  }
  return col;
}

void main() {
  vec2 uv = (gl_FragCoord.xy * 2.0 - resolution) / resolution.y;

  // Top energy bar
  vec4 top = drawArc(drawEnergyBar(uv, 1.0, false), false);
  float topPercent = min(1.0, energyPercent * 2.0);
  vec4 topEnergy = drawArc(drawEnergyBar(uv, topPercent, false), true);
  vec4 colT = mix(top, topEnergy, topPercent > 0.0 ? topEnergy.a : 0.0);

  // Bottom energy bar
  vec4 btm = drawArc(drawEnergyBar(uv, 1.0, true), false);
  float btmPercent = min(1.0, max(0.0, (energyPercent - 0.5) * 2.0));
  vec4 btmEnergy = drawArc(drawEnergyBar(uv, btmPercent, true), true);
  vec4 colB = mix(btm, btmEnergy, btmPercent > 0.0 ? btmEnergy.a : 0.0);

  // Mana bar
  vec4 mana = drawManaArc(drawManaBar(uv), false, uv);
  float mPerc = min(1.0, manaPercent);
  vec4 manaBlue = drawManaArc(drawManaBar(uv), true, uv);
  vec4 colM = mix(mana, manaBlue, mPerc > 0.0 ? manaBlue.a : 0.0);

  // Health bar
  vec4 health = drawHealthArc(drawHealthBar(uv), false, uv);
  float hPerc = min(1.0, healthPercent);
  vec4 healthGreen = drawHealthArc(drawHealthBar(uv), true, uv);
  vec4 colH = mix(health, healthGreen, hPerc > 0.0 ? healthGreen.a : 0.0);

  if (colT.a > 0.0) {
    gl_FragColor = colT;
    gl_FragColor.a *= 0.7;
  } else if (colB.a > 0.0) {
    gl_FragColor = colB;
    gl_FragColor.a *= 0.7;
  } else if (colM.a > 0.0) {
    gl_FragColor = colM;
    gl_FragColor.a *= 0.85;
  } else if (colH.a > 0.0) {
    gl_FragColor = colH;
    gl_FragColor.a *= 0.95;
  }
}

