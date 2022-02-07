#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
in vec4 aPosition;
in vec4 aTextureCoord;
out vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}
