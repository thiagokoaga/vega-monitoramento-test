import { useEffect, useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { ApiError, criarEntrada, listarDisponiveis, type CompartimentoDisponivel, type TipoIngrediente } from '@/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { COMPARTIMENTOS, TIPOS, lerQuantidade } from '@/format'

const RESPONSAVEL_KEY = 'armazem.responsavel'

type EntradaFormProps = {
  onSucesso: () => Promise<void>
}

export function EntradaForm({ onSucesso }: EntradaFormProps) {
  const [responsavel, setResponsavel] = useState(() => localStorage.getItem(RESPONSAVEL_KEY) ?? '')
  const [nome, setNome] = useState('')
  const [tipo, setTipo] = useState<TipoIngrediente>('SECO')
  const [quantidade, setQuantidade] = useState('')
  const [compartimentoId, setCompartimentoId] = useState('1')
  const [disponiveis, setDisponiveis] = useState<CompartimentoDisponivel[]>([])
  const [consulta, setConsulta] = useState<'vazia' | 'carregando' | 'pronta' | 'erro'>('vazia')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    localStorage.setItem(RESPONSAVEL_KEY, responsavel)
  }, [responsavel])

  useEffect(() => {
    const valor = lerQuantidade(quantidade)
    if (!valor) {
      setDisponiveis([])
      setConsulta('vazia')
      return
    }

    setConsulta('carregando')
    const timer = window.setTimeout(() => {
      listarDisponiveis(valor, tipo)
        .then((lista) => {
          setDisponiveis(lista)
          setConsulta('pronta')
        })
        .catch(() => {
          setDisponiveis([])
          setConsulta('erro')
        })
    }, 250)

    return () => window.clearTimeout(timer)
  }, [quantidade, tipo])

  async function enviar(event: FormEvent) {
    event.preventDefault()
    const valor = lerQuantidade(quantidade)
    if (!nome.trim()) {
      setErro('Informe o nome do ingrediente.')
      return
    }
    if (!valor) {
      setErro('Informe uma quantidade maior que zero.')
      return
    }
    if (!responsavel.trim()) {
      setErro('Informe o responsável.')
      document.getElementById('responsavel-entrada')?.focus()
      return
    }

    setEnviando(true)
    setErro(null)
    try {
      const ingrediente = await criarEntrada({
        nome: nome.trim(),
        tipo,
        quantidade: valor,
        compartimentoId: Number(compartimentoId),
        responsavel: responsavel.trim(),
      })
      toast.success(`${ingrediente.nome} atualizado em ${ingrediente.compartimentoCodigo}.`)
      setNome('')
      setQuantidade('')
      await onSucesso()
    } catch (causa) {
      setErro(causa instanceof ApiError ? causa.message : 'Não foi possível registrar a entrada.')
      await onSucesso()
    } finally {
      setEnviando(false)
    }
  }

  const codigosDisponiveis = disponiveis.map((item) => item.codigo)

  return (
    <Card id="entrada">
      <CardHeader>
        <CardTitle>Nova entrada</CardTitle>
        <CardDescription>Cadastra o ingrediente ou soma a quantidade se o nome já existir.</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="grid gap-4" onSubmit={enviar}>
          <div className="grid gap-2">
            <Label htmlFor="responsavel-entrada">Responsável</Label>
            <Input
              id="responsavel-entrada"
              value={responsavel}
              placeholder="Nome de quem está operando"
              onChange={(event) => setResponsavel(event.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="nome">Ingrediente</Label>
            <Input
              id="nome"
              value={nome}
              placeholder="Farinha de trigo"
              onChange={(event) => setNome(event.target.value)}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="grid gap-2">
              <Label>Tipo</Label>
              <Select value={tipo} onValueChange={(valor) => setTipo(valor as TipoIngrediente)}>
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent position="popper">
                  {TIPOS.map((item) => (
                    <SelectItem key={item.id} value={item.id}>
                      {item.nome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="quantidade-entrada">Quantidade</Label>
              <Input
                id="quantidade-entrada"
                inputMode="decimal"
                placeholder="0,000"
                value={quantidade}
                onChange={(event) => setQuantidade(event.target.value)}
              />
            </div>
          </div>
          <div className="grid gap-2">
            <Label>Compartimento</Label>
            <Select value={compartimentoId} onValueChange={setCompartimentoId}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent position="popper">
                {COMPARTIMENTOS.map((item) => (
                  <SelectItem key={item.id} value={String(item.id)}>
                    {item.codigo}
                    {consulta === 'pronta' && codigosDisponiveis.includes(item.codigo) ? ' · cabe agora' : ''}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-sm text-muted-foreground">
              {consulta === 'carregando'
                ? 'Consultando o espaço livre…'
                : consulta === 'erro'
                  ? 'Não foi possível consultar o espaço agora.'
                  : consulta === 'pronta'
                    ? codigosDisponiveis.length > 0
                      ? `Com espaço para esta entrada: ${codigosDisponiveis.join(', ')}.`
                      : 'Nenhum compartimento comporta essa quantidade hoje.'
                    : 'Informe a quantidade para ver onde ela cabe.'}
            </p>
          </div>
          {erro ? <p className="text-sm text-destructive">{erro}</p> : null}
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Registrar entrada'}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
