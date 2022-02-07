#version 300 es

precision mediump float;
in vec2 vTextureCoord;
uniform sampler2D sTexture;
out vec4 fragColor;

void main() {
    /* this one preserves the transparency component */
    vec4 texelColor = texture(sTexture, vTextureCoord);
    float grayScale = texelColor.r * 0.3 + texelColor.g * 0.59 + texelColor.b * 0.11;
    fragColor = vec4(grayScale, grayScale, grayScale, texelColor.a);
}
